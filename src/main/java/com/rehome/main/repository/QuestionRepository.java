package com.rehome.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rehome.main.entity.AdoptionQuestion;
import com.rehome.main.entity.Question;
import com.rehome.main.entity.Question.QuestionCategory;

/**
 * 問卷題目 Repository
 */
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    
    /**
     * 根據問卷分類查詢啟用的題目，並依排序欄位排序
     * 
     * @param category 問卷分類（adoption 或 surrender）
     * @return 啟用的問卷題目列表
     */
    @Query("SELECT q FROM Question q WHERE q.questionCategory = :category AND q.isActive = true ORDER BY q.sortOrder ASC")
    List<Question> findByQuestionCategoryOrderBySortOrderAsc(@Param("category") QuestionCategory category);
    
    /**
     * 查詢啟用的預設題目和指定類型的題目，並依排序欄位排序
     * 用於前端顯示時同時顯示預設題目和自訂題目
     * 
     * @param category 問卷分類（adoption 或 surrender）
     * @return 包含啟用的預設題目和指定分類題目的列表
     */
    @Query("SELECT q FROM Question q WHERE (q.questionCategory = :category OR q.questionCategory = com.rehome.main.entity.Question$QuestionCategory.default_) AND q.isActive = true ORDER BY q.sortOrder ASC")
    List<Question> findByQuestionCategoryIncludingDefaults(@Param("category") QuestionCategory category);
    
    /**
     * 根據問卷分類查詢題目數量
     * 
     * @param category 問卷分類
     * @return 題目數量
     */
    Long countByQuestionCategory(QuestionCategory category);

    @Query("""
        SELECT aq
        FROM AdoptionQuestion aq
        JOIN aq.question q
        WHERE aq.adoptionMember.id = :applicationId
          AND q.questionCategory = :category
    """)
    List<AdoptionQuestion> findByApplicationIdAndCategory(
        @Param("applicationId") Long applicationId,
        @Param("category") QuestionCategory category //問卷分類
    );
}
