/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.dto.response;

import java.util.List;

import lombok.Data;

/**
 * 會員中心 領養審核(會員TO會員) 按鈕-詳細資料 DTO
 * @author user
 */
@Data
public class AdoptionApplicationDetailDTO {
    
    private Long id;//申請ID
    private Long caseId;//案件ID

    private String contactName; //聯絡人姓名
    private String contactPhone;//聯絡人電話
    private String contactEmail;//聯絡人信箱
    // private String contactGender;//會員性別
    private String contactBirthday; //會員生日
    private String maritalStatus;//婚姻狀況
    private String employmentStatus;//就業狀況


    private String applicationDate; //申請日期

    private List<AdoptionQAItemDTO> qa; // 10 題


}
