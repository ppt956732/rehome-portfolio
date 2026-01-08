package com.rehome.main.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rehome.main.service.UpdateReviewAdoption;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 會員中心 審核領養(會員TO會員) - 按鈕動作 Controller
 * @author user
 */

// @RestController
// @RequestMapping("/api/process")
// @RequiredArgsConstructor
// public class AdoptionMemberActionController {
//     private final UpdateReviewAdoption updateReviewAdoption;

//      @PostMapping("/{id}/view-detail")
//     public ResponseEntity<?> viewDetail(@PathVariable("id") Long applicationId,
//                                         @RequestParam Long ownerMemberId) {
//         updateReviewAdoption.viewDetail(applicationId, ownerMemberId);
//         return ResponseEntity.ok().build();
//     }

//     @PostMapping("/{id}/open-chat")
//     public ResponseEntity<?> openChat(@PathVariable("id") Long applicationId,
//                                       @RequestParam Long ownerMemberId) {
//         updateReviewAdoption.openChat(applicationId, ownerMemberId);
//         return ResponseEntity.ok().build();
//     }

//     @PostMapping("/{id}/approve")
//     public ResponseEntity<?> approveApplication(@PathVariable("id") Long applicationId,
//                                      @RequestParam Long ownerMemberId) {
//         updateReviewAdoption.approveApplication(applicationId, ownerMemberId);
//         return ResponseEntity.ok().build();
//     }

//     @PostMapping("/{id}/reject")
//     public ResponseEntity<?> rejectApplication(@PathVariable("id") Long applicationId,
//                                     @RequestParam Long ownerMemberId) {
//         updateReviewAdoption.rejectApplication(applicationId, ownerMemberId);
//         return ResponseEntity.ok().build();
//     }

// }
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
public class AdoptionMemberActionController {

    private final UpdateReviewAdoption updateReviewAdoption;

    @PostMapping("/{id}/view-detail")
    public ResponseEntity<?> viewDetail(@PathVariable("id") Long applicationId,
                                        HttpServletRequest request) {

        Long ownerMemberId = (Long) request.getAttribute("memberId");
        if (ownerMemberId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "尚未登入或憑證已失效");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }

        updateReviewAdoption.viewDetail(applicationId, ownerMemberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/open-chat")
    public ResponseEntity<?> openChat(@PathVariable("id") Long applicationId,
                                      HttpServletRequest request) {

        Long ownerMemberId = (Long) request.getAttribute("memberId");
        if (ownerMemberId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "尚未登入或憑證已失效");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }

        updateReviewAdoption.openChat(applicationId, ownerMemberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveApplication(@PathVariable("id") Long applicationId,
                                                HttpServletRequest request) {

        Long ownerMemberId = (Long) request.getAttribute("memberId");
        if (ownerMemberId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "尚未登入或憑證已失效");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }

        updateReviewAdoption.approveApplication(applicationId, ownerMemberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectApplication(@PathVariable("id") Long applicationId,
                                               HttpServletRequest request) {

        Long ownerMemberId = (Long) request.getAttribute("memberId");
        if (ownerMemberId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "尚未登入或憑證已失效");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }

        updateReviewAdoption.rejectApplication(applicationId, ownerMemberId);
        return ResponseEntity.ok().build();
    }
}
