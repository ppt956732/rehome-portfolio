package com.rehome.main.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;


// 圖形數字驗證碼
@Service
public class MemCaptchaService {
    
    // 暫存驗證碼 + 過期時間
    private static class CaptchaData {
        String code;  // 驗證碼
        LocalDateTime expireTime;  // 過期時間

        CaptchaData(String code) {
            this.code = code;
            this.expireTime = LocalDateTime.now().plusMinutes(10); // 10分鐘後過期
        }
    }

    // 儲存 sessionId → CaptchaData
    private ConcurrentHashMap<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();
    
    // 儲存驗證碼
    public String storeCaptcha(String captchaCode) {
        String sessionId = UUID.randomUUID().toString();
        captchaStore.put(sessionId, new CaptchaData(captchaCode));
        return sessionId;
    }

    // 驗證驗證碼 12/16新增檢查
    public boolean verifyCaptcha(String sessionId, String userInput) {
        // 防禦性檢查：ConcurrentHashMap 不允許 null key，若 sessionId 為 null 會拋 NPE
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }

        CaptchaData data = captchaStore.get(sessionId);

        if (data == null) {
            return false; // 驗證碼不存在
        }
        
        // 檢查是否過期
        if (LocalDateTime.now().isAfter(data.expireTime)) {
            captchaStore.remove(sessionId);
            return false; // 已過期
        }
        
        // 驗證後移除（防止重複使用）
        captchaStore.remove(sessionId);

        // 若使用者輸入為 null 或空字串，視為驗證失敗
        if (userInput == null) {
            return false;
        }

        // 不區分大小寫比對
        return data.code.equalsIgnoreCase(userInput);
    }
}
