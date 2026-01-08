package com.rehome.main.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_refresh_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    private String refreshTokenHash;

    private LocalDateTime expiresAt;
    private boolean revoked;

    @Column(updatable = false, insertable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sessionExpiresAt;

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
