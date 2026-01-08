package com.rehome.main.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OpenDataSyncTaskTrigger implements CommandLineRunner {

    @Autowired
    private OpenDataSyncService openDataSyncService;

    /**
     * 觸發點 1: 每天早上 9:00 執行
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Taipei")
    public void scheduledTask() {
        log.info("觸發排程任務：每日 09:00 定期更新");
        openDataSyncService.executeSyncProcess();
    }

    /**
     * 觸發點 2: 伺服器每次開啟時執行
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("伺服器啟動：檢查今日資料更新狀態...");
        // openDataSyncService.executeSyncProcess();
    }
}
