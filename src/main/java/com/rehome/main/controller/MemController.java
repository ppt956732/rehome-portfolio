package com.rehome.main.controller;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rehome.main.dto.request.ForgotPasswordRequest;
import com.rehome.main.dto.request.MemAvatarRequest;
import com.rehome.main.dto.request.MemLoginRequest;
import com.rehome.main.dto.request.MemPasswordRequest;
import com.rehome.main.dto.request.MemRequest;
import com.rehome.main.dto.request.ResetPasswordRequest;
import com.rehome.main.dto.response.ApiResponse;
import com.rehome.main.dto.response.MemAvatarResponse;
import com.rehome.main.dto.response.MemLoginResponse;
import com.rehome.main.dto.response.MemResponse;
import com.rehome.main.dto.response.NavInfoDTO;
import com.rehome.main.service.MemCaptchaService;
import com.rehome.main.service.MemEmailService;
import com.rehome.main.service.MemService;
import com.rehome.main.service.MemberService;
import com.rehome.main.utils.MemApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/api/mem")
public class MemController {
    @Autowired
    private MemService memService;

    @Autowired
    private MemEmailService memEmailService;

    @Autowired
    private MemCaptchaService memCaptchaService;

    // ========== 註冊相關 API ==========
    
    // 會員註冊 12/15修改
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemResponse>> register(@RequestBody MemRequest request) {
        try {
            MemResponse response = memService.register(request);
            // 註冊成功
            return ResponseEntity.ok(ApiResponse.success("註冊成功", response));
        } catch (Exception e) {            
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(e.getMessage()));
        }
    }

    // ========== OTP相關 API ==========
    
    // 發送 OTP 12/15修改
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<?>> sendOtp(@RequestBody Map<String, String> request){
        try {
            String email = request.get("email");

            if(email == null || email.trim().isEmpty()){
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("Email 不能空白"));
            }

            memEmailService.memSendConfirmMail(email);

            return ResponseEntity.ok(ApiResponse.success("OTP 已發送至信箱 " ));

        }catch(Exception e){
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail("發送失敗 : " + e.getMessage()));
        }
    }

    // 驗證 OTP 12/15修改
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@RequestBody Map<String, String> request){
        try{
            String email = request.get("email");
            String otpCode = request.get("otpCode");

            // 基本驗證
            if(email == null || email.trim().isEmpty() || otpCode == null || otpCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("Email 或 驗證碼 不能空白"));
            }

            boolean isValid = memEmailService.memVerifyOtp(email, otpCode);

            if(isValid){
                return ResponseEntity.ok(ApiResponse.success("驗證成功"));
            }else{
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("驗證碼錯誤或已過期"));
            }
        
        }catch(Exception e){
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail("驗證失敗 : " + e.getMessage()));
        }
    }

    // ========== 會員中心基本資料相關 API ==========

    // 查詢會員基本資料 12/15修改
    @GetMapping("/profile")
    public ApiResponse<MemResponse> getProfile(@RequestParam Integer memberId) {

        MemResponse response  = memService.getMemberById(memberId);
        return ApiResponse.success(response);
    }

    // 更新會員暱稱 12/15修改
    @PutMapping("/update/nickName")
    public ApiResponse<MemResponse> updateNickName(@RequestParam Integer memberId, @RequestBody Map<String, String> request) {

        String nickName = request.get("nickName");

        MemResponse response = memService.updateNickName(memberId, nickName);

        return ApiResponse.success(response);
    }
    
    // 更新密碼 12/15修改
    @PutMapping("/update/password")
    public ResponseEntity<ApiResponse<MemResponse>> updatePassword(@RequestParam Integer memberId, @RequestBody MemPasswordRequest request) {

        try {
            if(request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("新密碼不能空白"));
            }else{
                memService.updatePassword(memberId, request.getOldPassword(), request.getNewPassword());
                return ResponseEntity.ok(ApiResponse.success("密碼更新成功", null));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(e.getMessage()));
        }
    }

    // ========== 登入相關 API ==========

    // 會員登入 12/15修改
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemLoginResponse>> login(@RequestBody MemLoginRequest request, HttpServletResponse response) {

        try {
            MemLoginResponse loginResponse = memService.login(request, response);
            
            // 登入成功
            return ResponseEntity.ok(
                ApiResponse.success("登入成功", loginResponse)
            );
            
        } catch (RuntimeException e) {
            // 業務邏輯錯誤 (驗證碼錯誤、帳密錯誤等)
            return ResponseEntity.status(401).body(
                ApiResponse.fail("驗證失敗: " + e.getMessage())
            );
        } catch (Exception e) {
            // 系統錯誤
            return ResponseEntity.status(500).body(
                ApiResponse.fail("登入失敗: " + e.getMessage())
            );
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<MemLoginResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        System.out.println(refreshToken);
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.fail("Refresh Token 缺失"));
        }

        return ResponseEntity.ok(
            ApiResponse.success(
                "Refresh 成功",
                memService.refreshToken(refreshToken, response)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        System.out.println(refreshToken);
        // 撤銷 refresh token
        memService.logout(refreshToken, response);

        return ResponseEntity.ok(ApiResponse.success("已登出"));
    }

    // 儲存驗證碼
    @PostMapping("/captcha/store")
    public ResponseEntity<ApiResponse<Map<String, Object>>> storeCaptcha(@RequestBody Map<String, String> request) {
        try {
            String captcha = request.get("captcha");

            if (captcha == null || captcha.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("captcha 不能為空"));
            }

            String sessionId = memCaptchaService.storeCaptcha(captcha);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.fail("儲存驗證碼失敗: " + e.getMessage()));
        }
    }

    // ========== 重設密碼相關 API ==========
    
    //忘記密碼 - 發送重設密碼郵件 12/15修改
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            // 驗證 Email
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("Email 不能空白"));
            }

            // 驗證圖形驗證碼
            if (!memCaptchaService.verifyCaptcha(request.getSessionId(), request.getCaptcha())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("驗證碼錯誤"));
            }
            
            // 處理忘記密碼請求
            memService.requestPasswordReset(request.getEmail());
            
            return ResponseEntity.ok(
                ApiResponse.success("重設密碼郵件已發送，請檢查您的信箱")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(e.getMessage()));
        }
    }
    
    //驗證重設密碼 Token 是否有效 12/15修改
    @GetMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<Boolean>> verifyResetToken(@RequestParam String token) {
        try {
            boolean isValid = memService.verifyResetToken(token);
            
            if (isValid) {
                return ResponseEntity.ok(
                    ApiResponse.success(true)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("Token 無效或已過期"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(e.getMessage()));
        }
    }
    
    //重設密碼 12/15修改
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("新密碼不能空白"));
            }

            // 驗證圖形驗證碼
            if (!memCaptchaService.verifyCaptcha(request.getSessionId(), request.getCaptcha())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("驗證碼錯誤"));
            }
            
            // 重設密碼
            memService.resetPassword(request.getToken(), request.getNewPassword());
            
            return ResponseEntity.ok(
                ApiResponse.success("密碼重設成功，請使用新密碼登入")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(e.getMessage()));
        }
    }

    // ========== 頭像暱稱相關 API ==========

    // 上傳會員頭像 12/15修改
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<MemAvatarResponse>> uploadAvatar(@RequestBody MemAvatarRequest request) {
        try {
            // 檢查會員
            if(request.getMemberId() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("會員ID不能為空"));
            }
            // 檢查頭像檔案
            if(request.getAvatar() == null ||request.getAvatar().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("頭像檔案不能為空"));
            }
            // 儲存頭像
            memService.saveAvatar(request.getMemberId(), request.getAvatar());
            return ResponseEntity.ok(ApiResponse.success("頭像上傳成功", null));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.fail("上傳失敗 : " + e.getMessage()));
        }
    }

    // 取得會員頭像/暱稱 12/15修改
    @GetMapping("/avatar")
    public ResponseEntity<ApiResponse<MemAvatarResponse>> getAvatar(@RequestParam Integer memberId) {
        try {
            // 檢查必要參數
            if (memberId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("會員ID不能為空"));
            }
            
            // 從 Service 取得頭像
            byte[] avatar = memService.getAvatar(memberId);
            
            // 取得會員暱稱
            String nickName = memService.getMemberById(memberId).getNickName();
            
            // 建立回應物件
            MemAvatarResponse response = new MemAvatarResponse();
            response.setAvatar(avatar);  // 如果沒有頭像,avatar 可能為 null
            response.setNickName(nickName);

            return ResponseEntity.ok(
                ApiResponse.success("取得會員資料成功", response)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.fail("取得會員資料失敗: " + e.getMessage()));
        }
    }

    @GetMapping("/navinfo")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> getNavProfile(HttpServletRequest request) {
        
        try {
            // 1. 從 Request 取出 memberId (這是 Object 型別)
            Object memberIdObj = request.getAttribute("memberId");

            // 2. 防呆判斷：如果 Filter 沒有成功解析 Token，這裡會是 null
            if (memberIdObj == null) {
                return ResponseEntity.ok(ApiResponse.fail("驗證資訊遺失，請重新登入"));
            }

            // 3. 安全轉型
            Long memberId = (Long) memberIdObj;

            // 4. 呼叫 Service (如果不該有的 ID 會在 Service 拋出例外)
            NavInfoDTO data = memService.getNavProfile(memberId);

            // 5. 回傳成功
            return ResponseEntity.ok(ApiResponse.success("取得資料成功", data));

        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.fail( e.getMessage()));
            
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.ok(ApiResponse.fail("系統發生未預期錯誤"));
        }
    }
}

