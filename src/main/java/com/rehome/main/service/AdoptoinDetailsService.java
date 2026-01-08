/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rehome.main.dto.response.AdoptionDetailsDTO;
import com.rehome.main.entity.Case;
import com.rehome.main.entity.PetInfo;
import com.rehome.main.repository.CaseStatusRepo;
import com.rehome.main.repository.PetCaseRep;
import com.rehome.main.repository.PetImageRep;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 *會員中心 - 刊登送養詳情
 * @author user
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdoptoinDetailsService {
    
    private final PetCaseRep petCaseRep;
    private final CaseStatusRepo caseStatusRepo;
    private final PetImageRep petImageRep;

    private static final Long ADOPTION_TYPE_ID = 2L;

    //會員中心-刊登送養詳情
    public List<AdoptionDetailsDTO> getAllAdoptionDetails(Long memberId) {

       
        List<Case> cases =  petCaseRep.findByMember_IdAndCaseType_Id(memberId, ADOPTION_TYPE_ID);

        return cases.stream().map(this::toCaseDto).toList();//語法糖
    }

    private AdoptionDetailsDTO toCaseDto(Case c) {
        AdoptionDetailsDTO dto = new AdoptionDetailsDTO();

        dto.setCaseId(c.getId());
        if (c.getMember() != null) {
            dto.setMemberId(c.getMember().getId());
        }
        dto.setCaseNumber(c.getCaseNumber());


        //寵物資訊
        PetInfo info = c.getPetInfo();
        if(info!=null){
            dto.setPetName(info.getName());
            dto.setPetGender(info.getGender());
            dto.setPetBreed(info.getBreed());
            if (info.getAnimalSpecies() != null) {
                dto.setPetType(info.getAnimalSpecies().getName());
            }
        }
        //建檔時間
        if (c.getCaseDateStart() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dto.setSubmitDate(c.getCaseDateStart().format(fmt));
        }

        //主要照片
       
        String photo = petImageRep.findFirstByPetCase_IdOrderBySortOrderAsc(c.getId())
            .filter(img -> img.getPhoto() != null && img.getPhoto().length > 0)
            .map(img -> "data:image/jpeg;base64," +
                java.util.Base64.getEncoder().encodeToString(img.getPhoto()))
            .orElse(null);

        dto.setPhoto(photo);

        //案件狀態
        if(c.getCaseStatus()!=null){
            dto.setCaseStatusId(c.getCaseStatus().getId());
            dto.setCaseStatusName(c.getCaseStatus().getName());
        }

        

        return dto;
    }

    //會員終止送養案件

     public void terminateAdoptionCase(Long memberId, Long caseId) {
        //找會員案件
        Case petCase = petCaseRep.findByIdAndMember_Id(caseId, memberId)
                .orElseThrow(() -> new RuntimeException("找不到案件資訊 " + caseId));
        //確認會員身份        
        Long ownerId = petCase.getMember().getId();
             if (!ownerId.equals(memberId)) {
                throw new RuntimeException("你沒有權限結束這個案件");
                }   


        // 找狀態
        Long TERMINATED_STATUS_ID = 4L;
        var terminatedStatus = caseStatusRepo.findById(TERMINATED_STATUS_ID)
                .orElseThrow(() -> new RuntimeException("找不到案件狀態 " + TERMINATED_STATUS_ID));

        petCase.setCaseStatus(terminatedStatus);
        petCase.setCaseDateEnd(LocalDateTime.now());
        petCaseRep.save(petCase);
        }



}
