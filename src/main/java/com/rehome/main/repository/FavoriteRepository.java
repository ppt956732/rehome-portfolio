package com.rehome.main.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rehome.main.dto.projection.FavoriteCaseDTO;
import com.rehome.main.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>, JpaSpecificationExecutor<Favorite> {
    // 用案件id 判斷狀態
    boolean existsByMemberIdAndPetCaseId(Long memberId, Long petCaseId);

    // 用案件編號 判斷狀態
    boolean existsByMemberIdAndPetCaseCaseNumber(Long memberId, String caseNumber);

    // 抓該會員的收藏 case id
    @Query("""
        SELECT f.petCase.id
        FROM Favorite f
        WHERE f.member.id = :memberId
        AND f.petCase.id IN :caseIds
    """)
    List<Long> findFavoriteCaseIds(
        @Param("memberId") Long memberId,
        @Param("caseIds") List<Long> caseIds
    );

    // 抓該會員的 所有收藏資料
    @Query(value = """
        SELECT new com.rehome.main.dto.projection.FavoriteCaseDTO(
            f.petCase.id,
            f.favoritesDate
        )
        FROM Favorite f
        WHERE f.member.id = :memberId
        AND f.petCase.caseType.id IN :caseTypeIds
        AND f.petCase.caseStatus.id IN :caseStatusIds
    """,
    countQuery = """
        SELECT COUNT(f)
        FROM Favorite f
        WHERE f.member.id = :memberId
          AND f.petCase.caseType.id IN :caseTypeIds
          AND f.petCase.caseStatus.id IN :caseStatusIds
    """)
    Page<FavoriteCaseDTO> findFavoriteByMemberId(
        @Param("memberId") Long memberId,
        @Param("caseTypeIds") List<Long> caseTypeIds,
        @Param("caseStatusIds") List<Long> caseStatusIds,
        Pageable pageable
    );


    // 因為用原生的砍不掉，所以就自定義方法
    @Modifying
    @Query("""
        DELETE FROM Favorite f WHERE f.member.id = :memberId AND f.petCase.caseNumber = :caseNumber
    """)
    int deleteByMemberIdAndPetCaseCaseNumber(@Param("memberId") Long memberId, @Param("caseNumber") String caseNumber);

}
