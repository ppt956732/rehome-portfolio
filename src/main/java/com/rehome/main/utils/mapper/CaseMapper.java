package com.rehome.main.utils.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.rehome.main.dto.response.CaseCardResponseDTO;
import com.rehome.main.dto.response.CaseContactResponseDTO;
import com.rehome.main.dto.response.CaseDetailResponseDTO;
import com.rehome.main.dto.response.CaseInfoResponseDTO;
import com.rehome.main.dto.response.CasePageResponseDTO;
import com.rehome.main.dto.response.PetInfoResponseDTO;
import com.rehome.main.entity.AdoptionMember;
import com.rehome.main.entity.AdoptionPetArea;
import com.rehome.main.entity.Case;
import com.rehome.main.entity.City;
import com.rehome.main.entity.Contact;
import com.rehome.main.entity.PetDetail;
import com.rehome.main.entity.PetImage;
import com.rehome.main.entity.PetInfo;
import com.rehome.main.entity.Region;
import com.rehome.main.entity.Shelter;
import com.rehome.main.repository.FavoriteRepository;

@Component
public class CaseMapper {
    @Autowired
    private FavoriteRepository favoriteRepository;

    public CaseCardResponseDTO toCaseCardDTO(Case entity, List<Long> favoriteCaseIds, boolean isAdoption) {
        CaseCardResponseDTO dto = new CaseCardResponseDTO();

        dto.setId(entity.getId());
        dto.setCaseNumber(entity.getCaseNumber());
        dto.setCaseDateStart(entity.getCaseDateStart());

        List<AdoptionMember> adoptionMembers = entity.getAdoptionMembers();

        dto.setIsFavorites(favoriteCaseIds == null ? false : favoriteCaseIds.contains(entity.getId()));
        dto.setIsPublic(entity.getCaseType().getId() == 3);
        dto.setIsOpen(adoptionMembers.stream().filter(Objects::nonNull).count() < 3);

        Optional.ofNullable(entity.getPetImage())          // 如果 list 為 null，就用 empty list
				.orElse(Collections.emptyList())
				.stream()
				.filter(Objects::nonNull)                   // 過濾 list 裡的 null 元素
				.filter(imageEntity -> imageEntity.getSortOrder() != null && imageEntity.getSortOrder() == 0)
				.findFirst()
				.ifPresent(imageEntity -> {
                    dto.setPhoto(imageEntity.getPhoto());
                    dto.setPhotoUrl(imageEntity.getPhotoUrl());
                });

        PetInfo petInfo = entity.getPetInfo();

        String species = Optional.ofNullable(petInfo.getAnimalSpeciesOther())
                .filter(s -> !s.isBlank()) 
                .orElse(petInfo.getAnimalSpecies().getName());

        dto.setPetName(petInfo.getName());
        dto.setSpecies(species);
        dto.setBreed(petInfo.getBreed());
        dto.setSize(petInfo.getSize());

        if (isAdoption) {
            Region region = petInfo.getRegion();
            City city = region.getCity();

            dto.setRegion(city.getName() + " " + region.getName());
        } else {
            PetDetail petDetail = entity.getPetDetail();
            Region region = petDetail.getRegion();
            City city = region.getCity();
            dto.setLostDate(petDetail.getLostDate());
            dto.setLostRegion(city.getName() + " " + region.getName());
        }

        return dto;
    }

    public CasePageResponseDTO toCasePageDTO(Case entity, Long memberId) {
        Long caseType = entity.getCaseType().getId();
        List<String> photoList = Optional.ofNullable(entity.getPetImage())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .<String>map(img -> { // 顯示指定類型為 <String>
                    if (img.getPhoto() != null && img.getPhoto().length > 0) {
                        // 如果是 BLOB，轉為 Base64 字串
                        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(img.getPhoto());
                    } else if (StringUtils.hasText(img.getPhotoUrl())) {
                        // 如果是 URL，直接回傳
                        return img.getPhotoUrl();
                    }
                    return null; // 兩者都沒則回傳 null，等一下會 filter 掉
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        CaseInfoResponseDTO caseInfo = CaseInfoResponseDTO.builder()
                .id(entity.getId())
                .caseNumber(entity.getCaseNumber())
                .caseDateStart(entity.getCaseDateStart())
                .isFavorites(favoriteRepository.existsByMemberIdAndPetCaseId(memberId, entity.getId()))
				.isMissing(caseType == 1)
                .isPublic(caseType == 3)
                .isOpen(entity.getAdoptionMembers().stream().filter(Objects::nonNull).count() < 3)
                .photo(photoList)
                .isAdoption(entity.getAdoptionMembers().stream().filter(Objects::nonNull).anyMatch(m -> m.getMember().getId().equals(memberId)))
                .isOwner(memberId == entity.getMember().getId())
                .build();

        Contact contactTemp = entity.getContact();
        CaseContactResponseDTO.CaseContactResponseDTOBuilder caseContactBuilder = CaseContactResponseDTO.builder()
                .isPhoneDisplay(contactTemp.getIsPhoneDisplay())
                .isEmailDisplay(contactTemp.getIsEmailDisplay());

        if (caseType != 3) {
            caseContactBuilder
                    .name(contactTemp.getName())
                    .tel(contactTemp.getTel())
                    .mail(contactTemp.getMail());
        } else {
            Shelter shelterTemp = entity.getContact().getShelter();
            caseContactBuilder
                    .name(shelterTemp.getName())
                    .tel(shelterTemp.getPhone())
                    .addr(shelterTemp.getAddress());
        }

        PetInfo petInfoTemp = entity.getPetInfo();
        Region regionTemp = petInfoTemp.getRegion();
        City cityTemp = regionTemp.getCity();
        PetInfoResponseDTO petInfo = PetInfoResponseDTO.builder()
                .petName(petInfoTemp.getName())
                .species(
                        Optional.ofNullable(petInfoTemp.getAnimalSpeciesOther())
                                .filter(s -> !s.isBlank()) 
                                .orElse(petInfoTemp.getAnimalSpecies().getName()))
                .breed(petInfoTemp.getBreed())
                .gender(petInfoTemp.getGender())
                .size(petInfoTemp.getSize())
                .age(petInfoTemp.getAge())
                .color(petInfoTemp.getColor())
                .feature(petInfoTemp.getFeature())
                .isEarTipping(petInfoTemp.getIsEarTipping())
                .isChip(petInfoTemp.getIsChip())
                .chipNumber(petInfoTemp.getChipNumber())
                .region(cityTemp.getName() + " " + regionTemp.getName())
                .build();

        PetDetail petDetailTemp = entity.getPetDetail();
        CaseDetailResponseDTO.CaseDetailResponseDTOBuilder detailBuilder = CaseDetailResponseDTO.builder()
                .description(petDetailTemp.getDescription());

        switch (caseType.intValue()) {
            case 1:
                Region lostRegionTemp = petDetailTemp.getRegion();
                City lostCityTemp = lostRegionTemp.getCity();
                detailBuilder.lostDetail(
                        CaseDetailResponseDTO.LostDetail.builder()
                                .lostDate(petDetailTemp.getLostDate())
                                .lostRegion(lostCityTemp.getName() + " " + lostRegionTemp.getName())
                                .lostAddr(petDetailTemp.getLostAddr())
                                .lng(petDetailTemp.getLng())
                                .lat(petDetailTemp.getLat())
                                .lostProcess(petDetailTemp.getLostProcess())
                                .build());
                break;
            case 2:
				List<AdoptionPetArea> adoptionPetAreas = entity.getAdoptionPetAreas();
				List<String> cityList = adoptionPetAreas.stream()
						.filter(Objects::nonNull)
						.map(item -> item.getCity().getName()).toList();
				
                detailBuilder.adoptionDetail(
                        CaseDetailResponseDTO.AdoptionDetail.builder()
                                .medicalInfo(petDetailTemp.getMedicalInfo())
                                .adoptionRequ(petDetailTemp.getAdoptionRequ())
                                .isFollowAger(petDetailTemp.getIsFollowAger())
                                .isFamilyAger(petDetailTemp.getIsFamilyAger())
                                .isAgeLimit(petDetailTemp.getIsAgeLimit())
								.cityList(cityList)
                                .build());
                break;
            case 3:
                detailBuilder.shelterDetail(
                        CaseDetailResponseDTO.ShelterDetail.builder()
                                .entryDates(petDetailTemp.getEntryDate())
                                .entryDays(ChronoUnit.DAYS.between(petDetailTemp.getEntryDate(), LocalDate.now()))
                                .foundPlace(petDetailTemp.getFoundPlace())
                                .build());
                break;

            default:
                break;
        }

        return CasePageResponseDTO.builder()
                .caseInfo(caseInfo)
                .caseContact(caseContactBuilder.build())
                .petInfo(petInfo)
                .detail(detailBuilder.build())
                .build();
    }
}
