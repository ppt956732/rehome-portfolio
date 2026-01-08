/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.dto.response;

import lombok.Data;

/**
 *會員中心 - 案件審核(會員TO會員) 長條摘要
 * @author user
 */
@Data
public class AdoptionCaseSummaryDTO {
    private Long caseId;
    private String caseNumber;
    private String submitDate;//刊登時間
    private String photo; //主要照片[1]
    private Integer count; //申請數量

}
