package com.rehome.main.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rehome.main.dto.request.MemLoginRequest;
import com.rehome.main.dto.request.MemRequest;
import com.rehome.main.dto.response.MemLoginResponse;
import com.rehome.main.dto.response.MemResponse;
import com.rehome.main.dto.response.NavInfoDTO;
import com.rehome.main.entity.MemEntity;
import com.rehome.main.entity.Member;
import com.rehome.main.entity.MemberRefreshToken;
import com.rehome.main.entity.PasswordResetTokenEntity;
import com.rehome.main.repository.MemRepository;
import com.rehome.main.repository.MemberRefreshTokenRepository;
import com.rehome.main.repository.MemberRepository;
import com.rehome.main.repository.PasswordResetTokenRepository;
import com.rehome.main.utils.MemJwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class MemService {
    
    @Autowired
    private MemRepository memRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemJwtUtil memJwtUtil;

    @Autowired
    private MemCaptchaService memCaptchaService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private MemEmailService memEmailService;

    @Autowired
    private MemberRefreshTokenRepository memberRefreshTokenRepository;
    
    // 會員註冊
    public MemResponse register(MemRequest request) {

        // 檢查 email 是否已存在
        if (memRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("此 Email 已被註冊");
        }
        
        // 建立 Entity 物件
        MemEntity entity = new MemEntity();
        entity.setEmail(request.getEmail());
        entity.setPassword(passwordEncoder.encode(request.getPassword())); // 加密密碼
        entity.setName(request.getName());
        entity.setGender(request.isGender());
        entity.setBirthDate(request.getBirthDate());
        entity.setPhone(request.getPhone());
        
        // 儲存到資料庫
        MemEntity savedEntity = memRepository.save(entity);
        
        // 轉換成 Response 回傳
        return convertToResponse(savedEntity);
    }
    
    // Entity 轉 Response DTO
    private MemResponse convertToResponse(MemEntity entity) {
        MemResponse response = new MemResponse();
        response.setId(entity.getId());
        response.setEmail(entity.getEmail());
        response.setName(entity.getName());
        response.setNickName(entity.getNickName());
        response.setGender(entity.getGender());
        response.setBirthDate(entity.getBirthDate());
        response.setPhone(entity.getPhone());
        return response;
    }

    // 根據會員ID查詢資料
    public MemResponse getMemberById(Integer memberId) {

        MemEntity entity = memRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("會員不存在"));

        return convertToResponse(entity);
    }

    // 基本資料 - 更新暱稱
    public MemResponse updateNickName(Integer memberId, String newNickName) {

        MemEntity entity = memRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("會員不存在"));

        entity.setNickName(newNickName);
        MemEntity updatedEntity = memRepository.save(entity);

        return convertToResponse(updatedEntity);
    }

    // 基本資料 - 更新密碼
    public void updatePassword(Integer memberId, String oldPassword, String newPassword) {

        MemEntity entity = memRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("會員不存在"));
        
        // 比對舊密碼
        if(!passwordEncoder.matches(oldPassword, entity.getPassword())){
            throw new RuntimeException("舊密碼不正確");
        }

        // 更新加密
        entity.setPassword(passwordEncoder.encode(newPassword));

        // 儲存
        memRepository.save(entity);
    }

    // ========== 登入token相關方法 ==========
    private ResponseCookie createAndSaveRefreshToken(Long memberId, LocalDateTime sessionExpiresAt) {
        // 產生新的 Refresh Token
        String refreshToken = memJwtUtil.generateRefreshToken();
        String refreshTokenHash = DigestUtils.sha256Hex(refreshToken);

        // Refresh Token 本身的有效期限（短）
        LocalDateTime refreshTokenExpiresAt = memJwtUtil.getRefreshTokenExpiryDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
                        
        // 存 DB
        MemberRefreshToken tokenEntity = MemberRefreshToken.builder()
                .memberId(memberId)
                .refreshTokenHash(refreshTokenHash)
                .expiresAt(refreshTokenExpiresAt)
                .sessionExpiresAt(sessionExpiresAt)     // ⭐ 關鍵
                .revoked(false)
                .build();

        memberRefreshTokenRepository.save(tokenEntity);

        // 生成 HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // 開發環境測試用，正式建議 true
                .path("/")
                .maxAge(Duration.ofMillis(memJwtUtil.getRefreshTokenExpiryDate().getTime() - System.currentTimeMillis()).getSeconds())
                .sameSite("Lax")
                .build();

        return cookie;
    }

    private void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) return;

        String refreshTokenHash = DigestUtils.sha256Hex(refreshToken);
        memberRefreshTokenRepository.findByRefreshTokenHash(refreshTokenHash)
                .ifPresent(tokenEntity -> {
                    tokenEntity.setRevoked(true);
                    memberRefreshTokenRepository.save(tokenEntity);
                });
    }

    // 登入
    @Transactional
    public MemLoginResponse login(MemLoginRequest request, HttpServletResponse response) {

        // 基本輸入檢查
        if (request.getCaptcha() == null || request.getCaptcha().trim().isEmpty()) {
            throw new RuntimeException("請輸入驗證碼");
        }
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new RuntimeException("sessionId 不能為空，請先向 /captcha/store 取得 sessionId 並一併送出");
        }

        // 驗證碼檢查
        if(!memCaptchaService.verifyCaptcha(request.getSessionId(), request.getCaptcha())){
            throw new RuntimeException("\n驗證碼錯誤，請刷新驗證碼後重新輸入");
        }

        // 根據 email 查找會員
        MemEntity member = memRepository.findByEmail(request.getEmail());
        // 檢查會員是否存在
        if(member == null) {
            throw new RuntimeException("會員不存在");
        }

        // 比對密碼
        if(!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("密碼錯誤");
        }

        // ===== 產生 Access Token =====
        String accessToken = memJwtUtil.generateAccessToken(
            member.getEmail(),
            member.getId(),
            member.getRole()
        );

        // ⭐ Session Hard Expiry
        LocalDateTime sessionExpiresAt = LocalDateTime.now().plusDays(30);

        ResponseCookie cookie = createAndSaveRefreshToken(member.getId().longValue(), sessionExpiresAt);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // ===== 回傳 =====
        return MemLoginResponse.builder()
                .role(member.getRole())
                .accessToken(accessToken)
                .build();
    }

    @Transactional
    public MemLoginResponse refreshToken(String refreshToken, HttpServletResponse response) {
        String refreshTokenHash = DigestUtils.sha256Hex(refreshToken);
        MemberRefreshToken tokenEntity = memberRefreshTokenRepository.findByRefreshTokenHash(refreshTokenHash)
                .filter(MemberRefreshToken::isValid)
                .orElseThrow(() -> new RuntimeException("Refresh Token 無效或已過期"));

        // ⭐ Session Hard Expiry 檢查（核心）
        if (LocalDateTime.now().isAfter(tokenEntity.getSessionExpiresAt())) {
            throw new RuntimeException("Session expired, please login again");
        }

        // 取得會員
        MemEntity member = memRepository.findById(tokenEntity.getMemberId().intValue())
                .orElseThrow(() -> new RuntimeException("會員不存在"));

        // ===== Rotation：撤銷舊 token =====
        revokeRefreshToken(refreshToken);

        // ===== 產生新 Access Token & Refresh Token =====
        String newAccessToken = memJwtUtil.generateAccessToken(
            member.getEmail(),
            member.getId(),
            member.getRole()
        );

        // ⭐ 新 Refresh Token「沿用同一個 sessionExpiresAt」
        ResponseCookie cookie = createAndSaveRefreshToken(member.getId().longValue(), tokenEntity.getSessionExpiresAt());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 回傳新的 access token
        return MemLoginResponse.builder()
                .role(member.getRole())
                .accessToken(newAccessToken)
                .build();
    }

    /**
     * 登出：撤銷 refresh token
     */
    @Transactional
    public void logout(String refreshToken, HttpServletResponse response) {
        // 撤銷 token
        revokeRefreshToken(refreshToken);

        // 清除 HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)      // 若用 https: true
                .path("/")
                .maxAge(0)         // 立即失效
                .sameSite("Lax") // https: Strict、本地測試用 Lax 避免跨站問題
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }


    // ========== 重設密碼相關方法 ==========
    
    //處理忘記密碼請求
    public void requestPasswordReset(String email) {
        
        // 檢查會員是否存在
        MemEntity member = memRepository.findByEmail(email);
        // 會員不存在
        if (member == null) {
            throw new RuntimeException("此 Email 尚未註冊");
        }
        
        // 發送重設密碼郵件
        memEmailService.sendPasswordResetEmail(email);
    }
    
    // 驗證重設密碼 Token 是否有效
    public boolean verifyResetToken(String token) {
        
        // 查詢 Token
        PasswordResetTokenEntity resetToken = 
            passwordResetTokenRepository.findByToken(token).orElse(null);
        // Token 不存在
        if (resetToken == null) {
            return false;
        }
        
        // Token 已被使用
        if (resetToken.isUsed()) {
            return false;
        }
        
        // Token 已過期
        if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
            return false;
        }
        // Token 有效
        return true;
    }
    
    // 重設密碼
    public void resetPassword(String token, String newPassword) {
        
        // 查詢 Token
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("無效的重設連結"));
        
        // 檢查是否已使用
        if (resetToken.isUsed()) {
            throw new RuntimeException("此重設連結已被使用");
        }
        
        // 檢查是否過期
        if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
            throw new RuntimeException("重設連結已過期，請重新申請");
        }
        
        // 查詢會員
        MemEntity member = memRepository.findByEmail(resetToken.getEmail());
        if (member == null) {
            throw new RuntimeException("會員不存在");
        }
        
        // 更新密碼
        member.setPassword(passwordEncoder.encode(newPassword));
        memRepository.save(member);
        
        // 標記 Token 為已使用
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    // 儲存會員頭像
    public void saveAvatar(Integer memberId, String base64Date) {
        // 查詢會員
        MemEntity member = memRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("會員不存在"));

        try {
            // 處理 Base64 字串
            String base64Image = base64Date;
            if(base64Date.contains(",")){
                base64Image = base64Date.split(",")[1];
            }

            // 解碼 Base64 字串
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);

            // 檢查檔案大小 (限制在 2MB 以內)
            if(imageBytes.length > 2 * 1024 * 1024) {
                throw new RuntimeException("頭像檔案大小不能超過 2MB");
            }

            // 儲存頭像
            member.setIcon(imageBytes);
            memRepository.save(member);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Base64 解碼失敗，請確認圖片格式正確");
        } catch (RuntimeException e) {
            // 重新拋出 RuntimeException（包括檔案大小限制）
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("頭像解碼失敗");
        }
    }

    // 取得會員頭像
    public byte[] getAvatar(Integer memberId) {
        // 查詢會員
        MemEntity member = memRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("會員不存在"));

        return member.getIcon();
    }

    @Transactional(readOnly = true) // 唯讀操作，優化效能
    public NavInfoDTO getNavProfile(Long memberId) {
        // 1. 查詢資料庫
        Optional<Member> memberOpt = memberRepository.findById(memberId);

        if (memberOpt.isEmpty()) {
            throw new RuntimeException("找不到會員資料"); 
            // 註：建議搭配自定義 Exception 或直接回傳 null 由 Controller 判斷
        }

        Member member = memberOpt.get();

        // 2. 處理圖片轉 Base64
        String photoBase64 = null;
        if (member.getIcon() != null && member.getIcon().length > 0) {
            photoBase64 = Base64.getEncoder().encodeToString(member.getIcon());
        }

        // 3. 回傳 DTO
        // 如果您也想回傳 memberId，可以在 DTO 裡多加一個欄位，這裡傳入 member.getId()
        return new NavInfoDTO(member.getName(), photoBase64);
    }
}

