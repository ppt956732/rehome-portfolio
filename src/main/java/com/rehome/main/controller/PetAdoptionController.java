/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.controller;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rehome.main.dto.request.PetinfoConDto;
import com.rehome.main.entity.Case;
import com.rehome.main.repository.PetCaseRep;
import com.rehome.main.service.PetIofnServer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


/**
 *
 * @author user
 */
@RestController
@RequestMapping("/api/se")
@RequiredArgsConstructor
public class PetAdoptionController {

    private final PetCaseRep petCaseRep;
    private final PetIofnServer petIofnServer;


    /*
     * 提交寵物認養資料
     * POST /api/se/submit-pet-adoption  
     *  Case c = new Case();
     */
    @PostMapping("/submit-pet-adoption")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> submitPetAdoption(
        @RequestBody PetinfoConDto infDto,
            HttpServletRequest request) {
    Long memberId = (Long) request.getAttribute("memberId");


        if (memberId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "尚未登入或憑證已失效");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        try {
            Case savedCase = petIofnServer.create(infDto,memberId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "資料已成功寫入資料庫");
            response.put("caseId", savedCase.getId()); 
            response.put("caseNumber", savedCase.getCaseNumber());   
            response.put("caseDate", savedCase.getCaseDateStart().toString());     
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "資料寫入失敗: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /*
     * 取得寵物認養案件審核結果
     * GET /api/se/pet-cases/{id}/review-result
     */
    @GetMapping("/pet-cases/{id}/review-result")
     @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
        public ResponseEntity <Map<String, Object>>getReviewResult(@PathVariable("id")  Long caseId,
        HttpServletRequest request) {
            Long memberId = (Long) request.getAttribute("memberId");
             if (memberId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "尚未登入或憑證已失效"));
    }


        Case Case = petCaseRep.findById(caseId)
            .orElseThrow(() -> new RuntimeException("案件不存在: " + caseId));
        
        Long ownerId = Case.getMember().getId();
            if (!ownerId.equals(memberId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "你沒有權限查看這個案件"));
            }


        Map<String, Object> response = new HashMap<>();
        response.put("caseId", Case.getId());
        response.put("caseNumber", Case.getCaseNumber());
        response.put("status", Case.getCaseStatus().getName());

        var status = Case.getCaseStatus();

        boolean approved = (status.getId() == 2L); //成功
        boolean rejected = (status.getId() == 3L);  //失敗

        response.put("approved", approved);
        response.put("rejected", rejected);

        return ResponseEntity.ok(response);
    }
        
}
