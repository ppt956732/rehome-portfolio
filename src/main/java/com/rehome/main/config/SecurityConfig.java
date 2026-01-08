package com.rehome.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 啟用方法級別的安全控制
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 設定
            .csrf(csrf -> csrf.disable())
            
            // Session 管理 (JWT 模式下使用 STATELESS)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                )
            )
            
            // 授權設定
            .authorizeHttpRequests(auth -> auth
                 // ===== 公開 API =====
                // 公開端點 - 不需認證
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/verify-account",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/auth/refresh-token",
                    "/api/qna/all",
                    "/api/qna/random/**",
                    "/api/cs/send",
                    "/api/se/animal-species",//物種
                    "/api/se/cities",//城市
                    "/api/se/regions",//區域
                    "/api/public/options",
                    "/api/cases/**",
                    "/api/mem/**",
                    "/api/banners/active",
                    "/api/members/missing/applications",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/se/**"
                ).permitAll()

                // ===== 靜態資源 =====
                // 靜態資源 - 必須開放，否則無法載入登入頁面
                .requestMatchers(
                    "/static/**",
                    "/",
                    // "/**",
                    "/index.html",
                    "/frontend/**",
                    "/admin/**",
                    "/assets/**",
                    "/content/**",
                    "/fragments/**",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/*.html"
                ).permitAll()
                
                // ===== 後台管理 =====
                // API 端點權限控制（正式環境）
                 // 暫時改為只需認證，用於測試
                .requestMatchers(
                    "/api/admin/**",
                    "/api/dashboard/**",
                    "/api/statistics/**",
                    "/api/review/**",
                    // "/api/members/**",
                    "/api/banners/**",
                    // "/api/survey/**"
                    "/api/cases/admin/**"
                ).hasRole("ADMIN")
                
                // ===== 會員 API =====
                .requestMatchers("/api/member/**").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers("/api/members/**").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers("/api/survey/**").hasAnyRole("MEMBER", "ADMIN")                
                .requestMatchers("/api/se/submit-pet-adoption").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers("/api/pu/member/adoptions/**").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers("/api/se/pet-cases/*/review-result").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers("/api/ado/**").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers("/api/process/**").hasAnyRole("MEMBER", "ADMIN")

                // ===== 其餘 API =====
                .requestMatchers("/api/**").authenticated()  // 其他 API 需要認證
                
                // ===== 其他 =====
                // 其他所有端點需要認證
                .anyRequest().authenticated()
            )
            
            // 加入 JWT 過濾器 (在 UsernamePasswordAuthenticationFilter 之前執行)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 12/5 OAuth2 第三方登入
            .oauth2Login(oauth -> oauth
                .defaultSuccessUrl("/mem/oauth2/login-success", true)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

