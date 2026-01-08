package com.rehome.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rehome.main.dto.request.CaseFormRequestDTO;
import com.rehome.main.dto.response.ApiResponse;
import com.rehome.main.service.CaseService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cases")
public class CaseController {
    @Autowired
    private CaseService caseService;

    @GetMapping("{type}/home")
    public ResponseEntity<ApiResponse<?>> getHome(@PathVariable String type, HttpServletRequest httprequest) {
        if (!"adoption".equals(type) && !"missing".equals(type)) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail("Invalid type. Only 'adoption' and 'missing' are allowed."));
        }
        Boolean isAdoption = "adoption".equals(type);

        Long memberId = (Long) httprequest.getAttribute("memberId");

        return ResponseEntity.ok(
                ApiResponse.success(caseService.getHomeInfo(memberId, isAdoption)));
    }

    @GetMapping("/options")
    public ResponseEntity<ApiResponse<?>> getOptions() {

        return ResponseEntity.ok(
                ApiResponse.success(caseService.getOptions()));
    }

    @PostMapping("{type}/search")
    public ResponseEntity<ApiResponse<?>> postSearch(
        @RequestBody CaseFormRequestDTO caseFormRequestDTO,
        @PathVariable String type, 
        HttpServletRequest httprequest) {
        if (!"adoption".equals(type) && !"missing".equals(type)) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail("Invalid type. Only 'adoption' and 'missing' are allowed."));
        }
        Boolean isAdoption = "adoption".equals(type);

        Long memberId = (Long) httprequest.getAttribute("memberId");

        return ResponseEntity.ok(
                ApiResponse.success(caseService.getSearchCardList(caseFormRequestDTO, memberId, isAdoption)));
    }

    @GetMapping("/{caseNumber}")
    public ResponseEntity<ApiResponse<?>> getDetail(@PathVariable String caseNumber, HttpServletRequest httprequest) {
        Long memberId = (Long) httprequest.getAttribute("memberId");

        return ResponseEntity.ok(
                ApiResponse.success(caseService.getCasePage(caseNumber, memberId, false)));
    }

    @GetMapping("/admin/{caseNumber}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAdminDetail(@PathVariable String caseNumber, HttpServletRequest httprequest) {
        Long memberId = (Long) httprequest.getAttribute("memberId");
        // 雙重保險：雖然 PreAuthorize 擋在前面，但防呆還是檢查一下
        if (memberId == null) {
            return ResponseEntity.ok(ApiResponse.fail("認證資訊錯誤：找不到會員ID"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(caseService.getCasePage(caseNumber, memberId, true)));
    }
}
