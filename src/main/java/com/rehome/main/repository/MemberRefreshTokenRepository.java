package com.rehome.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rehome.main.entity.MemberRefreshToken;

public interface MemberRefreshTokenRepository extends JpaRepository<MemberRefreshToken, Long> {
    Optional<MemberRefreshToken> findByRefreshTokenHash(String hash);

    List<MemberRefreshToken> findByMemberIdAndRevokedFalse(Long memberId);

    void deleteByMemberId(Long memberId);
}
