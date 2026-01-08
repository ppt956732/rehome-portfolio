/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.rehome.main.dto.response.AdoptionApplicationSummaryDTO;
import com.rehome.main.dto.response.AdoptionCaseSummaryDTO;
import com.rehome.main.dto.response.AdoptionCaseWithApplicationsDTO;
import com.rehome.main.entity.AdoptionMember;
import com.rehome.main.entity.Case;
import com.rehome.main.repository.AdoptionMemberRepository;
import com.rehome.main.repository.PetCaseRep;
import com.rehome.main.repository.PetImageRep;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 會員中心 - 審核送養(會員to會員) 長條摘要 及 申請人列表List Service
 * @author user
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewAdoptionDetails {

    private final PetCaseRep petCaseRep;
    private final AdoptionMemberRepository adoptionMemberRepository;
    private final PetImageRep petImageRep;

    
    public List<AdoptionCaseWithApplicationsDTO> getMyCasesWithTop3Apps(Long memberId) {
        Long ADOPTION_TYPE_ID = 2L;
        List<Case>  cases = petCaseRep.findByMember_IdAndCaseType_IdOrderByCaseDateStartDesc(memberId,ADOPTION_TYPE_ID);

     return cases.stream().map(c -> {

            // 案件摘要
            AdoptionCaseSummaryDTO caseInfo = new AdoptionCaseSummaryDTO();
           caseInfo.setCaseId(c.getId());
            caseInfo.setCaseNumber(c.getCaseNumber());
            // 刊登日期
            if (c.getCaseDateStart() != null) {
                caseInfo.setSubmitDate(c.getCaseDateStart().toLocalDate().toString());
            }
            // 申請人數
            int count = (int) adoptionMemberRepository.countByPetCase_Id(c.getId());
            caseInfo.setCount(count);
            // 主要照片
            String photo = petImageRep.findFirstByPetCase_IdOrderBySortOrderAsc(c.getId())
                    .filter(img -> img.getPhoto() != null && img.getPhoto().length > 0)
                    .map(img -> "data:image/jpeg;base64," +
            java.util.Base64.getEncoder().encodeToString(img.getPhoto())).orElse(null);


            caseInfo.setPhoto(photo);


            // 申請摘要列表 (前3筆)

            List<AdoptionMember> top3Apps = adoptionMemberRepository.findByPetCase_IdOrderByCreatedAtDesc(c.getId(), PageRequest.of(0, 3));
            //
            List<AdoptionApplicationSummaryDTO> appDtos = top3Apps.stream()
                    .map(this::toApplicationSummaryDTO)
                    .toList();
            AdoptionCaseWithApplicationsDTO out = new AdoptionCaseWithApplicationsDTO();
            out.setCaseInfo(caseInfo);
            out.setApplications(appDtos);

            return out;

        }).toList();
    }
    //申請 會員摘要
    private AdoptionApplicationSummaryDTO toApplicationSummaryDTO(AdoptionMember app) {
        AdoptionApplicationSummaryDTO dto = new AdoptionApplicationSummaryDTO();
        //
        dto.setApplicationId(app.getId());
        dto.setApplicantMemberId(app.getMember() != null ? app.getMember().getId() : null);
        //申請人名稱
        if(app.getMember() != null){
            dto.setName(app.getMember().getName());

            // String nickname = safeStr(app.getMember().getNickName());
            //  String name = safeStr(app.getMember().getName());
            // dto.setName(!nickname.isBlank() ? nickname : name);
        }
        //申請日期
        dto.setDate(app.getCreatedAt() == null ? null : app.getCreatedAt().toLocalDate().toString());
        //申請狀態
        if (app.getAdoptionStatus() != null) {
            dto.setStatusId(app.getAdoptionStatus().getId());
            dto.setStatus(app.getAdoptionStatus().getName());
            } 
        return dto;
    }

        // private String safeStr(String s) {
        // return s == null ? "" : s.trim();
        // }
}


