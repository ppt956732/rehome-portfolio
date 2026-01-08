/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rehome.main.dto.response.AdoptionCaseWithApplicationsDTO;
import com.rehome.main.service.ReviewAdoptionDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 *會員中心 審核領養(會員TO會員) 審核長條 申請人列表 Controller
 * @author user
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ado")
public class MemberAdoptionCenterController {

    private final ReviewAdoptionDetails reviewAdoptionDetails;
    
    // @GetMapping("/adoption-cases")
    // public ResponseEntity<List<AdoptionCaseWithApplicationsDTO>> myAdoptionCases() {
    //     Long memberId = 1L; // TODO: 換成 JWT 取得登入會員
    //     return ResponseEntity.ok(reviewAdoptionDetails.getMyCasesWithTop3Apps(memberId));

    // }

    
     @GetMapping("/adoption-cases")
    public ResponseEntity<List<AdoptionCaseWithApplicationsDTO>> myAdoptionCases(
            HttpServletRequest request
    ) {
        Long memberId = (Long) request.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewAdoptionDetails.getMyCasesWithTop3Apps(memberId));
    }
}


