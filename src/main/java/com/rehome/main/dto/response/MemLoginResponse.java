package com.rehome.main.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemLoginResponse {
    private String role;
    private String accessToken; // 登入成功後的 accessToken
}