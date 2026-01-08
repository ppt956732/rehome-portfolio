package com.rehome.main.scheduled;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rehome.main.dto.schedule.OpenDataDTO;
import com.rehome.main.dto.schedule.SyncResult;
import com.rehome.main.entity.AnimalSpecies;
import com.rehome.main.entity.Case;
import com.rehome.main.entity.CaseStatus;
import com.rehome.main.entity.CaseType;
import com.rehome.main.entity.Contact;
import com.rehome.main.entity.Member;
import com.rehome.main.entity.PetDetail;
import com.rehome.main.entity.PetImage;
import com.rehome.main.entity.PetInfo;
import com.rehome.main.entity.Region;
import com.rehome.main.entity.Shelter;
import com.rehome.main.repository.AnimalSpeciesRepo;
import com.rehome.main.repository.CaseRepository;
import com.rehome.main.repository.CaseStatusRepo;
import com.rehome.main.repository.CaseTypeRepo;
import com.rehome.main.repository.MemberRepository;
import com.rehome.main.repository.RegionRepo;
import com.rehome.main.repository.ShelterRepository;

import io.swagger.v3.oas.annotations.servers.Server;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CaseProcessor {
    @Autowired
    private CaseAtomService caseAtomService;
    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CaseStatusRepo caseStatusRepository;
    @Autowired
    private CaseTypeRepo caseTypeRepository;
    @Autowired
    private AnimalSpeciesRepo animalSpeciesRepository;
    @Autowired
    private RegionRepo regionRepository;
    @Autowired
    private ShelterRepository shelterRepository;

    public SyncResult process() throws Exception {
        String url = "https://data.moa.gov.tw/Service/OpenData/TransService.aspx?UnitId=QcbUEzN6E6DL&IsTransData=1";

        // 抓取 API (使用 RestClient)
        // ParameterizedTypeReference 讓 RestClient 直接將 JSON 轉成 List<DTO>
        List<OpenDataDTO> apiList = RestClient.create().get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OpenDataDTO>>() {});

        if (apiList == null || apiList.isEmpty()) {
            throw new RuntimeException("API 未回傳任何資料");
        }

        // 快取現有資料進行比對 (避免 N+1 查詢問題)
        // 假設你的 Case 透過 PetInfo 關聯 animalId
        List<String> existingList = caseRepository.findAllWithCaseType();

        // 在 process() 方法開始時
        Member member = memberRepository.findById(1L).orElse(null);
        Map<String, AnimalSpecies> speciesMap = animalSpeciesRepository.findAll()
                .stream().collect(Collectors.toMap(AnimalSpecies::getName, s -> s, (a, b) -> a));
        Map<String, Shelter> shelterMap = shelterRepository.findAll()
                .stream().collect(Collectors.toMap(Shelter::getName, s -> s, (a, b) -> a));

        int successCount = 0;
        // 4. 比對並儲存
        for (OpenDataDTO dto : apiList) {
            try {
                if (existingList.contains(dto.getCaseNumber())) {
                    // 已存在 -> 更新
                    // updateExistingCase(existingMap.get(dto.getAnimalId()), dto);
                } else if (LocalDate.now().isAfter(dto.getOpendate())) {
                    // 不存在 -> 新增
                    Case newCase = createNewCase(dto, member, speciesMap, shelterMap);
                    caseAtomService.saveSingleCase(newCase);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("處理編號 {} 失敗: {}", dto.getCaseNumber(), e.getMessage());
            }
        }

        return new SyncResult(apiList.size(), successCount);
    }

    // private void updateExistingCase(Cases existing, OpenDataDTO dto) {
    //     // 更新狀態、備註等欄位
    //     existing.getPetInfo().setStatus(dto.getAnimalStatus());
    //     // 如果有變動才存檔，節省資料庫效能
    //     casesRepository.save(existing);
    // }

    private Case createNewCase(OpenDataDTO dto, Member member, Map<String, AnimalSpecies> speciesMap, Map<String, Shelter> shelterMap) {
        Case newCase = new Case();

        // 1. 設定基本資料與會員 FK
        newCase.setMember(member);
        newCase.setCaseDateStart(dto.getCreateAt().atStartOfDay());

        // 2. 設定案件狀態 (預設: 審核成功) 與 類型 (收容)
        CaseStatus initialStatus = caseStatusRepository.findById(2L) // id=2 是 "審核成功"
                .orElseThrow(() -> new RuntimeException("找不到初始案件狀態"));
        CaseType type = caseTypeRepository.findById(3L) // 3 是收容
                .orElseThrow(() -> new RuntimeException("找不到案件類型"));

        newCase.setCaseStatus(initialStatus);
        newCase.setCaseType(type);

        // 3. 案號
        newCase.setCaseNumber(dto.getCaseNumber());

        // 4. 處理 PetInfo (基本資料)
        PetInfo petInfo = new PetInfo();
        String petName = dto.getColor() + AnimalSize.fromCode(dto.getSize()) + AnimalSexC.fromCode(dto.getGender()) + dto.getSpecies();
        petInfo.setName(petName);
        petInfo.setGender(AnimalSexE.fromCode(dto.getGender()));
        petInfo.setBreed(dto.getBreed());
        petInfo.setColor(dto.getColor());
        petInfo.setSize(dto.getSize());
        petInfo.setAge(StringUtils.hasText(dto.getAge()) ? dto.getAge() : "adult");

        // 使用傳入的 Map 避開重複 SQL 查詢
        Shelter shelter = shelterMap.get(dto.getShelterName());
        if (shelter == null) {
            log.warn("跳過案件：找不到名為 {} 的收容所", dto.getShelterName());
            return null; // 回傳 null 讓外層迴圈跳過
        }

        petInfo.setRegion(shelter.getRegion());
        // ==================================
        String typeStr = dto.getSpecies();

        if (typeStr != null && !typeStr.isEmpty()) {
            try {

                // 2. 查詢資料庫 (findById 回傳的是 Optional<AnimalSpecies>)
                AnimalSpecies species = speciesMap.get(typeStr);
                if (species == null) {
                    log.warn("跳過案件：找不到名為 {} 的物種", typeStr);
                    return null; // 回傳 null 讓外層迴圈跳過
                }

                // 3. 塞入物件
                petInfo.setAnimalSpecies(species);

            } catch (Exception e) {
                // 防止前端傳了 "abc" 這種無法轉成數字的字串
                throw new RuntimeException("動物種類錯誤: " + typeStr);
            }
        } else {
            // 若沒傳 ID 的處理邏輯 (例如設為 null 或預設值)
            petInfo.setAnimalSpecies(null);
        }

        // 處理 Boolean/String 轉換 (假設前端傳 "yes"/"no")
        petInfo.setIsEarTipping("T".equalsIgnoreCase(dto.getSterilization()));
        petInfo.setIsChip(false);
        // petInfo.setChipNumber(request.getPetChipNumber());

        petInfo.setPetCase(newCase); // 雙向關聯
        newCase.setPetInfo(petInfo);

        // 5. 處理 PetDetail
        PetDetail petDetail = new PetDetail();
        petDetail.setFoundPlace(dto.getFoundPlace());
        petDetail.setEntryDate(dto.getCreateAt());
        petDetail.setDescription(dto.getRemark());

        petDetail.setPetCase(newCase);
        newCase.setPetDetail(petDetail);

        // 6. 處理 Contact (聯絡人)
        Contact contact = new Contact();
        contact.setShelter(shelter);
        // 其他欄位如 isPhoneDisplay 若前端有傳需 set
        contact.setPetCase(newCase);
        newCase.setContact(contact);

        // 7. 處理 PetImage (圖片 Base64 解碼)
        List<PetImage> images = new ArrayList<>();
        PetImage perImage = new PetImage();
        perImage.setPhotoUrl(dto.getImageUrl());
        perImage.setSortOrder(0);
        perImage.setPetCase(newCase);
        images.add(perImage);

        newCase.setPetImage(images);

        return newCase;
    }

    @Getter
    @AllArgsConstructor
    public enum AnimalSexC {
        M("公"), 
        F("母"), 
        N("");

        private final String chinese;
        // 只要一行 Stream 就能處理所有轉換邏輯
        public static String fromCode(String code) {
            return Stream.of(values())
                    .filter(e -> e.name().equalsIgnoreCase(code))
                    .findFirst()
                    .map(AnimalSexC::getChinese)
                    .orElse("");
        }
    }

    @Getter
    @AllArgsConstructor
    public enum AnimalSexE {
        M("male"), 
        F("female"), 
        N("unknown");

        private final String english;
        // 只要一行 Stream 就能處理所有轉換邏輯
        public static String fromCode(String code) {
            return Stream.of(values())
                    .filter(e -> e.name().equalsIgnoreCase(code))
                    .findFirst()
                    .map(AnimalSexE::getEnglish)
                    .orElse("unknown");
        }
    }

    @Getter
    @AllArgsConstructor
    public enum AnimalSize {
        SMALL("小型"), 
        MEDIUM("中型"), 
        BIG("大型");

        private final String chinese;
        // 只要一行 Stream 就能處理所有轉換邏輯
        public static String fromCode(String code) {
            return Stream.of(values())
                    .filter(e -> e.name().equalsIgnoreCase(code))
                    .findFirst()
                    .map(AnimalSize::getChinese)
                    .orElse("一般");
        }
    }
}
