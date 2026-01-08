/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rehome.main.dto.response.AdoptionApplicationDetailDTO;
import com.rehome.main.dto.response.AdoptionQAItemDTO;
import com.rehome.main.entity.AdoptionMember;
import com.rehome.main.entity.AdoptionQuestion;
import com.rehome.main.entity.Question.QuestionCategory;
import com.rehome.main.repository.AdoptionMemberRepository;
import com.rehome.main.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

/**
 * 會員中心 領養審核 - 申請詳情 Service --按鈕-詳細資料
 * @author user
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdoptionApplicationDetailService {
    private final AdoptionMemberRepository adoptionMemberRepository;
    private final QuestionRepository QuestionRepository;

    public AdoptionApplicationDetailDTO dto (Long applicationId, Long ownerMemberId) {
        //
        AdoptionMember app = adoptionMemberRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("找不到申請紀錄 " + applicationId));

        Long caseOwnerId = app.getPetCase().getMember().getId();
        if (!caseOwnerId.equals(ownerMemberId)) {
            throw new RuntimeException("你沒有權限查看這筆申請");
        }
        

        AdoptionApplicationDetailDTO dto = new AdoptionApplicationDetailDTO();
        // 案件基本資料
        dto.setId(app.getId()); //申請ID
        dto.setCaseId(app.getPetCase().getId());

        // 聯絡人資料 - 加入空值檢查
        if (app.getPetCase() != null && app.getPetCase().getContact() != null) {
            dto.setContactName(app.getMember().getName());//聯絡人姓名
            dto.setContactPhone(app.getMember().getPhone());//聯絡人電話
            dto.setContactEmail(app.getMember().getEmail());//聯絡人信箱
        }
        
        dto.setMaritalStatus(app.getMaritalStatus());//婚姻狀況
        dto.setEmploymentStatus(app.getEmploymentStatus());//就業狀況

        // 申請日期
        if (app.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dto.setApplicationDate(app.getCreatedAt().format(formatter)); //申請日期
        }
        // 取得問答題目與答案
       List<AdoptionQuestion> questions =
       QuestionRepository.findByApplicationIdAndCategory(
            app.getId(),
            QuestionCategory.adoption //只抓領養問卷 不然會炸掉
    );

        List<AdoptionQAItemDTO> qa = questions.stream()
            .sorted(Comparator.comparing(AdoptionQuestion::getId))
            .map(q -> {
                AdoptionQAItemDTO item = new AdoptionQAItemDTO();
                if (q.getQuestion() != null) {
                    item.setQuestion(q.getQuestion().getQuestion());
                }
                item.setAnswer(q.getAnswer());
                return item;
    })
    .toList();

dto.setQa(qa);



        return dto;
    }
        
}
