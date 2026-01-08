package com.rehome.main.entity;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 問卷題目實體
 */
@Entity
@Table(name = "question")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "question", nullable = false, length = 200)
    private String question;
    
    @Column(name = "content", length = 500)
    private String content;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @Convert(converter = QuestionCategoryConverter.class)
    @Column(name = "question_category", columnDefinition = "ENUM('adoption', 'surrender', 'default')")
    private QuestionCategory questionCategory;
    
    @Column(name = "update_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private boolean isActive;
    
    /**
     * 問卷分類枚舉
     */
    public enum QuestionCategory {
        adoption,   // 領養問卷
        surrender,  // 送養問卷
        default_    // 預設題目（資料庫中為 'default'）
    }
    
    /**
     * QuestionCategory Enum 與資料庫值的轉換器
     * 將 Java 的 default_ 映射到資料庫的 'default'
     */
    @Converter(autoApply = true)
    public static class QuestionCategoryConverter implements AttributeConverter<QuestionCategory, String> {

        @Override
        public String convertToDatabaseColumn(QuestionCategory category) {
            if (category == null) {
                return null;
            }
            // 將 default_ 轉換為 'default'
            if (category == QuestionCategory.default_) {
                return "default";
            }
            return category.name();
        }

        @Override
        public QuestionCategory convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            // 將資料庫的 'default' 轉換為 default_
            if ("default".equals(dbData)) {
                return QuestionCategory.default_;
            }
            return QuestionCategory.valueOf(dbData);
        }
    }
}
