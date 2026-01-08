package com.rehome.main.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rehome.main.dto.request.SurveyRequest;
import com.rehome.main.dto.response.ApiResponse;
import com.rehome.main.dto.response.SurveyResponse;
import com.rehome.main.entity.Question;
import com.rehome.main.entity.Question.QuestionCategory;
import com.rehome.main.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 問卷服務
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {

    private final QuestionRepository questionRepository;

    /**
     * 根據問卷類型獲取題目列表
     * 
     * @param type 問卷類型（adoption 或 surrender）
     * @return 問卷題目列表
     */
    public ApiResponse<SurveyResponse.QuestionList> getQuestions(String type) {
        try {
            // 驗證問卷類型
            QuestionCategory category = validateAndConvertType(type);
            if (category == null) {
                return ApiResponse.fail("無效的問卷類型，僅支援 adoption 或 surrender");
            }

            // 查詢題目列表（包含預設題目和指定類型的題目）
            List<Question> questions = questionRepository.findByQuestionCategoryIncludingDefaults(category);
            
            // 轉換為 Response DTO
            List<SurveyResponse.QuestionItem> questionItems = questions.stream()
                    .map(this::convertToQuestionItem)
                    .collect(Collectors.toList());

            // 獲取最近更新時間
            LocalDateTime lastUpdated = questions.stream()
                    .map(Question::getUpdatedAt)
                    .filter(updatedAt -> updatedAt != null)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            SurveyResponse.QuestionList result = SurveyResponse.QuestionList.builder()
                    .type(type)
                    .questions(questionItems)
                    .totalCount(questionItems.size())
                    .lastUpdated(lastUpdated)
                    .build();

            return ApiResponse.success("取得問卷題目成功", result);

        } catch (Exception e) {
            log.error("取得問卷題目失敗: {}", e.getMessage(), e);
            return ApiResponse.fail("系統錯誤，請稍後再試");
        }
    }

    /**
     * 儲存問卷題目列表
     * 
     * @param request 儲存請求
     * @return 儲存結果
     */
    public ApiResponse<SurveyResponse.QuestionList> saveQuestions(SurveyRequest.SaveQuestions request) {
        // 驗證問卷類型（在事務外進行驗證）
        QuestionCategory category = validateAndConvertType(request.getType());
        if (category == null) {
            return ApiResponse.fail("無效的問卷類型，僅支援 adoption 或 surrender");
        }

        try {
            return saveQuestionsInTransaction(request, category);
        } catch (Exception e) {
            log.error("儲存問卷題目失敗 - 類型: {}, 錯誤訊息: {}", 
                    request.getType(), e.getMessage(), e);
            return ApiResponse.fail("系統錯誤：" + e.getMessage());
        }
    }

    /**
     * 在事務中儲存問卷題目
     */
    @Transactional
    private ApiResponse<SurveyResponse.QuestionList> saveQuestionsInTransaction(
            SurveyRequest.SaveQuestions request, QuestionCategory category) {
        try {
            log.info("開始儲存問卷 - 類型: {}, 題目數量: {}", request.getType(), request.getQuestions().size());

            // 先查詢現有題目
            List<Question> existingQuestions = questionRepository.findByQuestionCategoryOrderBySortOrderAsc(category);
            log.info("找到現有題目數量: {}", existingQuestions.size());
            
            List<Question> savedQuestions = new java.util.ArrayList<>();
            
            // 更新或新增題目
            for (SurveyRequest.QuestionItem item : request.getQuestions()) {
                Question question;
                
                if (item.getId() != null && item.getId() > 0) {
                    // 有 ID 則更新現有題目
                    Integer questionId = item.getId();
                    question = questionRepository.findById(questionId)
                            .orElseThrow(() -> new IllegalArgumentException("題目 ID " + questionId + " 不存在"));
                    
                    // 如果是預設題目（question_category = 'default'），只更新排序
                    if (question.getQuestionCategory() == QuestionCategory.default_) {
                        question.setSortOrder(item.getSortOrder());
                        log.info("更新預設題目排序 ID: {}", question.getId());
                    } else {
                        // 自訂題目可以全部更新
                        question.setQuestion(item.getQuestion());
                        question.setContent(item.getContent());
                        question.setSortOrder(item.getSortOrder());
                        log.info("更新自訂題目 ID: {}", question.getId());
                    }
                    question.setUpdatedAt(LocalDateTime.now());
                } else {
                    // 沒有 ID 則新增題目
                    question = convertToEntity(item, category);
                    question.setActive(true); // 新增題目預設為啟用
                    question.setUpdatedAt(LocalDateTime.now());
                    log.info("新增題目: {}", item.getQuestion());
                }
                
                savedQuestions.add(questionRepository.save(question));
            }
            
            // 停用不在請求中的自訂題目（軟刪除）
            List<Integer> requestIds = request.getQuestions().stream()
                    .map(SurveyRequest.QuestionItem::getId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            
            List<Question> toDeactivate = existingQuestions.stream()
                    .filter(q -> q.getQuestionCategory() != QuestionCategory.default_) // 只停用非預設題目
                    .filter(q -> !requestIds.contains(q.getId()))
                    .collect(Collectors.toList());
            
            if (!toDeactivate.isEmpty()) {
                log.info("準備停用 {} 個自訂題目", toDeactivate.size());
                for (Question q : toDeactivate) {
                    q.setActive(false);
                    q.setUpdatedAt(LocalDateTime.now());
                    questionRepository.save(q);
                }
                log.info("成功停用 {} 個題目", toDeactivate.size());
            }

            log.info("成功儲存 {} 個題目", savedQuestions.size());

            // 重新查詢以獲取最新的 updated_at
            savedQuestions = questionRepository.findByQuestionCategoryOrderBySortOrderAsc(category);

            // 轉換為 Response DTO
            List<SurveyResponse.QuestionItem> questionItems = savedQuestions.stream()
                    .map(this::convertToQuestionItem)
                    .collect(Collectors.toList());

            // 獲取最近更新時間
            LocalDateTime lastUpdated = savedQuestions.stream()
                    .map(Question::getUpdatedAt)
                    .filter(updatedAt -> updatedAt != null)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());

            SurveyResponse.QuestionList result = SurveyResponse.QuestionList.builder()
                    .type(request.getType())
                    .questions(questionItems)
                    .totalCount(questionItems.size())
                    .lastUpdated(lastUpdated)
                    .build();

            return ApiResponse.success("問卷儲存成功", result);

        } catch (Exception e) {
            log.error("事務內儲存問卷失敗: {}", e.getMessage(), e);
            throw e; // 重新拋出異常以觸發事務回滾
        }
    }

    /**
     * 驗證並轉換問卷類型
     * 
     * @param type 問卷類型字串
     * @return QuestionCategory 或 null（無效類型）
     */
    private QuestionCategory validateAndConvertType(String type) {
        try {
            return QuestionCategory.valueOf(type.toLowerCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 將 Entity 轉換為 Response DTO
     * 
     * @param question 問卷題目 Entity
     * @return QuestionItem DTO
     */
    private SurveyResponse.QuestionItem convertToQuestionItem(Question question) {
        return SurveyResponse.QuestionItem.builder()
                .id(question.getId())
                .question(question.getQuestion())
                .content(question.getContent())
                .sortOrder(question.getSortOrder())
                .isDefault(question.getQuestionCategory() == QuestionCategory.default_)
                .build();
    }

    /**
     * 將 Request DTO 轉換為 Entity
     * 
     * @param item 問卷題目 Request DTO
     * @param category 問卷分類
     * @return Question Entity
     */
    private Question convertToEntity(SurveyRequest.QuestionItem item, QuestionCategory category) {
        return Question.builder()
                .question(item.getQuestion())
                .content(item.getContent())
                .sortOrder(item.getSortOrder())
                .questionCategory(category)
                // 不設定 updatedAt，讓資料庫自動處理
                .build();
    }
}
