package com.rehome.main.scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rehome.main.dto.schedule.SyncResult;
import com.rehome.main.entity.OpendataImportLog;
import com.rehome.main.repository.OpendataImportLogRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpenDataSyncService {
    @Autowired
    private OpendataImportLogRepository opendataImportLogRepository;
    @Autowired
    private CaseProcessor caseProcessor;

    @Transactional
    public void executeSyncProcess() {
        LocalDate today = LocalDate.now();

        // 判斷今日是否已成功執行過
        if (opendataImportLogRepository.existsByImportDate(today)) {
            log.info("今日 ({}) 已有執行紀錄，跳過同步。", today);
            return;
        }

        OpendataImportLog opendataImportLog = new OpendataImportLog();
        opendataImportLog.setImportDate(today);
        opendataImportLog.setExecutionTime(LocalDateTime.now());
        opendataImportLog.setStatus("RUNNING");

        try {
            // 1. 執行抓取與比對邏輯 (需回傳處理筆數與備份路徑)
            // 假設 processor.process() 執行：抓取 -> 備份 -> 逐筆 saveAll()
            SyncResult result = caseProcessor.process(); 

            opendataImportLog.setStatus("SUCCESS");
            opendataImportLog.setTotalCount(result.getTotal());
            opendataImportLog.setSuccessCount(result.getSuccess());
            // opendataImportLog.setBackupFilePath(result.getFilePath());
            log.info("資料同步成功：共 {} 筆", result.getSuccess());

        } catch (Exception e) {
            opendataImportLog.setStatus("FAILED");
            opendataImportLog.setErrorMessage(e.getMessage());
            log.error("資料同步失敗：{}", e.getMessage());
        } finally {
            opendataImportLogRepository.save(opendataImportLog);
        }
    }
}
