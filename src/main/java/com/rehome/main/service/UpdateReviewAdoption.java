/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rehome.main.entity.AdoptionMember;
import com.rehome.main.entity.AdoptionStatus;
import com.rehome.main.repository.AdoptionMemberRepository;
import com.rehome.main.repository.AdoptionStatusReopsitory;

import lombok.RequiredArgsConstructor;

/**
 *  會員中心 領養審核 (會員TO會員)- 更新申請狀態 Service
 * @author user
 */

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateReviewAdoption {

        private final AdoptionMemberRepository adoptionMemberRepository;
        private final AdoptionStatusReopsitory adoptionStatusRepository;

        private static final Long STATUS_CONFIRM_DATA = 1L; // 確認資料
        private static final Long STATUS_MATCH_1 = 2L;// 媒合期1
        private static final Long STATUS_MATCH_2 = 3L;// 媒合期2
        private static final Long STATUS_APPROVED = 4L; // 同意送養
        private static final Long STATUS_REJECTED = 5L; // 不同意

        private AdoptionMember getAndCheckOwner(Long applicationId, Long ownerMemberId) {
            // 取得申請紀錄
            AdoptionMember adoptionMember = adoptionMemberRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("找不到申請紀錄 " + applicationId));
            // 檢查是否為該案件擁有者
            if (!adoptionMember.getPetCase().getMember().getId().equals(ownerMemberId)) {
                throw new RuntimeException("無權限修改此申請紀錄 " + applicationId);
              }
            return adoptionMember;
        }


            // 確認案件未結案
        private void ensureNotClosed(Long cur) {
            if (cur.equals(STATUS_APPROVED) || cur.equals(STATUS_REJECTED)) {
            throw new RuntimeException("此申請已結案，不能再操作");
                }
        }
            // 設定狀態並儲存
        private void setStatus(AdoptionMember app, Long statusId) {
            AdoptionStatus status = adoptionStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("找不到狀態 id=" + statusId));
            app.setAdoptionStatus(status);
            // adoptionMemberRepository.save(app);
         }


            // 確認資料--->媒合期1
        public void viewDetail(Long applicationId, Long ownerMemberId) {
            AdoptionMember app = getAndCheckOwner(applicationId, ownerMemberId);
            Long cur = app.getAdoptionStatus().getId();

            ensureNotClosed(cur);

            if (cur.equals(STATUS_CONFIRM_DATA)) {
            setStatus(app, STATUS_MATCH_1);
            }
        }

        // 媒合期1--->媒合期2
        public void openChat(Long applicationId, Long ownerMemberId) {
            AdoptionMember app = getAndCheckOwner(applicationId, ownerMemberId);
            Long cur = app.getAdoptionStatus().getId();

            ensureNotClosed(cur);
            if (!cur.equals(STATUS_MATCH_1)) {
            throw new RuntimeException("目前狀態無法開啟聊天室");
            }   
            setStatus(app, STATUS_MATCH_2);
        }

        // 媒合期2---->同意送養
        public void approveApplication(Long applicationId, Long ownerMemberId) {
            AdoptionMember app = getAndCheckOwner(applicationId, ownerMemberId);
            Long cur = app.getAdoptionStatus().getId();

            ensureNotClosed(cur);
            if (!cur.equals(STATUS_MATCH_2)) {
            throw new RuntimeException("目前狀態無法同意送養");
            }   
            setStatus(app, STATUS_APPROVED);
            app.setEndAt(LocalDateTime.now());
            adoptionMemberRepository.save(app);
        }

        // 不同意送養，狀態改為「不同意」
        public void rejectApplication(Long applicationId, Long ownerMemberId) {
            AdoptionMember app = getAndCheckOwner(applicationId, ownerMemberId);
            Long cur = app.getAdoptionStatus().getId();

            ensureNotClosed(cur);

            setStatus(app, STATUS_REJECTED);
            app.setEndAt(LocalDateTime.now());
            adoptionMemberRepository.save(app);
        }
    }



    