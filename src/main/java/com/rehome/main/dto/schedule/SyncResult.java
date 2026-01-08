package com.rehome.main.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncResult {
    private int total;      // API 抓到的總數
    private int success;    // 成功存入的數量
    // private String filePath; // 本地備份 JSON 的路徑
}
