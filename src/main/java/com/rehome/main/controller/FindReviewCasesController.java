/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rehome.main.dto.response.PetReviewDto;
import com.rehome.main.service.FindReviewCasesService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;



/**
 *送養 案件完整資料（審核頁用）
 * @author user
 */
@RestController
@RequestMapping("/api/se/")
@RequiredArgsConstructor
public class FindReviewCasesController {

   private final FindReviewCasesService reviewCasesService;

   @GetMapping("/adoption-cases/{caseId}")
    public ResponseEntity<Map<String, Object>> getCaseDetail(@PathVariable Long caseId,HttpServletRequest request) {

         Long memberId = (Long) request.getAttribute("memberId");
        if (memberId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "尚未登入"));
    }
        try {
            PetReviewDto dto = reviewCasesService.getCaseDetail(caseId);
            
            if (!dto.getMemberId().equals(memberId)) {
                 return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "你沒有權限查看這個案件"));
    }
            
            // 建立符合前端期待的回應格式
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            response.put("message", "成功獲取案件資料");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 錯誤回應格式
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("data", null);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    


}
