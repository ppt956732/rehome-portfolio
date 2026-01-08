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

import com.rehome.main.dto.response.AdoptionApplicationDetailDTO;
import com.rehome.main.service.AdoptionApplicationDetailService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * // 會員中心 領養審核(會員TO會員) - 申請詳情 Controller --按鈕-詳細資料
 * @author user
 */
@RestController
@RequestMapping("/api/ado")
@RequiredArgsConstructor
public class AdoptionApplicationDetailController {
    private final AdoptionApplicationDetailService detailService;


     @GetMapping("/{id}/detail")
public ResponseEntity<?> getDetail(
        @PathVariable("id") Long applicationId,
        HttpServletRequest request) {

    Long ownerMemberId = (Long) request.getAttribute("memberId");

    // 沒登入 → 401
    if (ownerMemberId == null) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "尚未登入或憑證已失效");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    // 交給 service（裡面仍會檢查是不是該案件主人）
    AdoptionApplicationDetailDTO dto = detailService.dto(applicationId, ownerMemberId);

    return ResponseEntity.ok(dto);
}

}
