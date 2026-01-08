/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.dto.response;

import lombok.Data;

/**
 *會員中心 - 審核領養(會員TO會員) 申請列表 DTO
 * @author user
 */
@Data
public class AdoptionApplicationSummaryDTO {
    private Long applicationId;      // 申請ID
    private Long applicantMemberId; // 申請人會員ID

    private String name;    // 姓名 or 暱稱 領養者
    private String date;  // 申請時間
    private String icon; //頭像 擴充?

    private Long statusId;//狀態4種
    private String status; //狀態文字
}
