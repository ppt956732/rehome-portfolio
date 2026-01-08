package com.rehome.main.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rehome.main.dto.request.LostNotificationFormDTO;
import com.rehome.main.dto.response.ApiResponse;
import com.rehome.main.service.MemberMissingService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/members/missing")
public class MemberMissingController {

    @Autowired
    private MemberMissingService memberMissingService;

    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<?>> postApplications(@RequestBody LostNotificationFormDTO dto) {

        return ResponseEntity.ok(
                memberMissingService.saveLostNotification(dto));
    }

    @GetMapping("/applications/{caseNumber}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> getApplications(
            @PathVariable String caseNumber,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size, HttpServletRequest httprequest) {
        
        Long memberId = (Long) httprequest.getAttribute("memberId");
        // 雙重保險：雖然 PreAuthorize 擋在前面，但防呆還是檢查一下
        if (memberId == null) {
            return ResponseEntity.ok(ApiResponse.fail("認證資訊錯誤：找不到會員ID"));
        }
        System.out.println(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        memberMissingService.findLostNotification(memberId, caseNumber, page, size)));
    }

}
