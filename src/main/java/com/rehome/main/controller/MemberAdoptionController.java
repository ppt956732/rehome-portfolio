package com.rehome.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rehome.main.dto.request.MemberAdoptionFormDTO;
import com.rehome.main.dto.response.ApiResponse;
import com.rehome.main.service.MemberAdoptionService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/members/adoption")
public class MemberAdoptionController {

    @Autowired
    private MemberAdoptionService memberAdoptionService;

    @PostMapping("/applications")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> postApplications(
            @RequestBody MemberAdoptionFormDTO dto,
            HttpServletRequest httprequest) {
        
        Long memberId = (Long) httprequest.getAttribute("memberId");
        // 雙重保險：雖然 PreAuthorize 擋在前面，但防呆還是檢查一下
        if (memberId == null) {
            return ResponseEntity.ok(ApiResponse.fail("認證資訊錯誤：找不到會員ID"));
        }

        return ResponseEntity.ok(
                memberAdoptionService.saveAdoptionForm(dto, memberId));
    }

    @GetMapping("/applications")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> getApplications(HttpServletRequest httprequest) {
        
        Long memberId = (Long) httprequest.getAttribute("memberId");
        // 雙重保險：雖然 PreAuthorize 擋在前面，但防呆還是檢查一下
        if (memberId == null) {
            return ResponseEntity.ok(ApiResponse.fail("認證資訊錯誤：找不到會員ID"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(memberAdoptionService.getAdoptionList(memberId)));
    }

    @DeleteMapping("/applications/{caseNumber}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteApplications(@PathVariable String caseNumber, HttpServletRequest httprequest) {
        
        Long memberId = (Long) httprequest.getAttribute("memberId");
        // 雙重保險：雖然 PreAuthorize 擋在前面，但防呆還是檢查一下
        if (memberId == null) {
            return ResponseEntity.ok(ApiResponse.fail("認證資訊錯誤：找不到會員ID"));
        }

        if (memberAdoptionService.deleteAdoptionInfo(caseNumber, memberId)) {
            return ResponseEntity.ok(ApiResponse.success("刪除領養成功"));
        } else {
            return ResponseEntity.ok(ApiResponse.fail("刪除領養失敗"));
        }
    }

}
