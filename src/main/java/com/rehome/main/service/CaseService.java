package com.rehome.main.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rehome.main.dto.request.CaseFormRequestDTO;
import com.rehome.main.dto.response.CaseCardResponseDTO;
import com.rehome.main.dto.response.CasePageResponseDTO;
import com.rehome.main.dto.response.OptionsResponseDTO;
import com.rehome.main.dto.response.PaginationResponseDTO;
import com.rehome.main.entity.Case;
import com.rehome.main.repository.CaseRepository;
import com.rehome.main.repository.CityRepository;
import com.rehome.main.repository.FavoriteRepository;
import com.rehome.main.repository.ShelterRepository;
import com.rehome.main.repository.SpeciesRepository;
import com.rehome.main.specification.CaseCardSpecification;
import com.rehome.main.utils.mapper.CaseMapper;

@Service
public class CaseService {

    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private ShelterRepository shelterRepository;
    @Autowired
    private SpeciesRepository speciesRepository;
	@Autowired
	private FavoriteRepository favoriteRepository;

    @Autowired
    private CaseMapper caseMapper;

    @Transactional(readOnly = true)
	// @Cacheable("homeCardList")
    public List<CaseCardResponseDTO> getHomeInfo(Long memberId, Boolean isAdoption) {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("caseDateStart").descending());
        List<Long> caseTypeIds = isAdoption ? Arrays.asList(2L, 3L) : Arrays.asList(1L);

		return caseRepository.findCaseCards(memberId, caseTypeIds, 2L, isAdoption, pageable);
    }

    // city、shelter、species
    @Transactional(readOnly = true)
	@Cacheable("options")
    public OptionsResponseDTO getOptions() {
        List<OptionsResponseDTO.CityDTO> cityDTOs = cityRepository.findAll().stream()
                .map(city -> OptionsResponseDTO.CityDTO.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .build())
                .collect(Collectors.toList());

        List<OptionsResponseDTO.ShelterDTO> shelterDTOs = shelterRepository.findAll().stream()
                .map(shelter -> OptionsResponseDTO.ShelterDTO.builder()
                        .id(shelter.getId())
                        .name(shelter.getName())
                        .build())
                .collect(Collectors.toList());

        return OptionsResponseDTO.builder()
                .citys(cityDTOs)
                .shelters(shelterDTOs)
                .species(speciesRepository.findAll())
                .build();
    }

    
    @Transactional(readOnly = true)
	// @Cacheable("searchCardList")
    public PaginationResponseDTO<CaseCardResponseDTO> getSearchCardList(CaseFormRequestDTO dto, Long memberId, Boolean isAdoption) {
        Pageable pageable = PageRequest.of(
                dto.getPagination().getPage() - 1,
                dto.getPagination().getLimit(),
                Sort.by(dto.getPagination().getSortOrder().equalsIgnoreCase("desc") ? Sort.Direction.DESC
                        : Sort.Direction.ASC, "caseDateStart"));

        Page<Case> cases = caseRepository.findAll(CaseCardSpecification.searchCardList(dto), pageable);
        List<Long> favoriteCaseIds = getFavoriteCaseIds(cases.toList(), memberId);

        Page<CaseCardResponseDTO> result = cases.map(entity -> caseMapper.toCaseCardDTO(entity, favoriteCaseIds, isAdoption));

        return PaginationResponseDTO.fromPage(result);
    }

    @Transactional(readOnly = true)
	// @Cacheable("casePage")
    public CasePageResponseDTO getCasePage(String caseNumber, Long memberId, boolean isAdmin) {
        return caseMapper.toCasePageDTO(
                caseRepository.findByCaseNumberAndCaseStatusId(caseNumber, isAdmin ? 1L : 2L),
                memberId);
    }

    public List<Long> getFavoriteCaseIds(List<Case> cases, Long memberId) {
        // 用 case id list 來找尋是否有收藏
        List<Long> caseIds = cases.stream().map(entity -> entity.getId()).toList();
        return favoriteRepository.findFavoriteCaseIds(memberId, caseIds);
    }
}
