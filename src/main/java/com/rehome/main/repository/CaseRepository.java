package com.rehome.main.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rehome.main.dto.response.CaseCardResponseDTO;
import com.rehome.main.entity.Case;

public interface CaseRepository extends JpaRepository<Case, Long>, JpaSpecificationExecutor<Case> {
    // home EntityGraph + stream => JPQL + DTO + subquery
    @Query("""
        select new com.rehome.main.dto.response.CaseCardResponseDTO(
            c.id,
            c.caseNumber,
            c.caseDateStart,
            (case when f.id is not null then true else false end),
            (case when c.caseType.id = 3 then true else false end),
            (size(c.adoptionMembers) < 3),
            pi.photo,
            pi.photoUrl,
            p.name,
            coalesce(nullif(trim(p.animalSpeciesOther), ''), a.name),
            p.breed,
            p.size,
            (case when :isAdoption = true
                then concat(city.name, ' ', r.name)
                else null end),
            (case when :isAdoption = false
                then pd.lostDate
                else null end),
            (case when :isAdoption = false
                then concat(pdCity.name, ' ', pdRegion.name)
                else null end)
        )
        from Case c
        left join c.petInfo p
        left join p.animalSpecies a
        left join p.region r
        left join r.city city
        left join c.petDetail pd
        left join pd.region pdRegion
        left join pdRegion.city pdCity
        left join c.petImage pi on pi.sortOrder = 0
        left join Favorite f on (
            :memberId IS NOT NULL
            AND f.member.id = :memberId
            AND f.petCase.id = c.id)
        where c.caseType.id in :caseTypeIds
        and c.caseStatus.id = :caseStatusId
        order by c.caseDateStart desc
    """)
    List<CaseCardResponseDTO> findCaseCards(
        @Param("memberId") Long memberId,
        @Param("caseTypeIds") List<Long> caseTypeIds,
        @Param("caseStatusId") Long caseStatusId,
        @Param("isAdoption") boolean isAdoption,
        Pageable pageable
    );


    // CaseCardList 複雜搜尋
    @Override
    @EntityGraph(attributePaths = {
        "caseType", 
        "petInfo", 
        "petInfo.animalSpecies", 
        "petInfo.region", 
        "petInfo.region.city",
        "petDetail",
        "petDetail.region", 
        "petDetail.region.city", 
        "contact",
        "contact.shelter",
        "petImage",
        "adoptionMembers"
    })
    Page<Case> findAll(Specification<Case> spec, Pageable pageable);

    // CasePage
    @EntityGraph(attributePaths = {
        "caseType", 
        "petInfo", 
        "petInfo.animalSpecies", 
        "petInfo.region", 
        "petInfo.region.city",
        "petDetail",
        "petDetail.region", 
        "petDetail.region.city", 
        "contact",
        "contact.shelter",
        "petImage",
        "adoptionMembers",
        "adoptionPetAreas",
        "adoptionPetAreas.city"
    })
    Case findByCaseNumberAndCaseStatusId(String caseNumber, Long caseStatusId);

    Case getReferenceByCaseNumber(String caseNumber);
    
    // Dashboard 統計方法
    Long countByCaseTypeId(Long caseTypeId);
    
    Long countByCaseTypeIdAndCaseDateStartBetween(Long caseTypeId, LocalDateTime start, LocalDateTime end);
    
    Long countByCaseTypeIdAndCaseDateEndBetween(Long caseTypeId, LocalDateTime start, LocalDateTime end);

    Optional<Case> findByCaseNumber(String caseNumber);

    // MemberCenter CaseCardList
    @EntityGraph(attributePaths = {
        "caseType", 
        "petInfo", 
        "petInfo.animalSpecies", 
        "petInfo.region", 
        "petInfo.region.city",
        "petDetail",
        "petDetail.region", 
        "petDetail.region.city", 
        "contact",
        "contact.shelter",
        "petImage",
        "adoptionMembers"
    })
    List<Case> findByIdIn(List<Long> ids);

    // 排程使用，抓取所有caseNumber
    @Query("SELECT c.caseNumber FROM Case c JOIN c.caseType ct ON ct.id = 3")
    List<String> findAllWithCaseType();
}
