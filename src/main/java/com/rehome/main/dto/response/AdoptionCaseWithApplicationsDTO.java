/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.rehome.main.dto.response;

import java.util.List;

import lombok.Data;

/**
 *  會員中心 - 一個案件 + 其申請清單 (審核頁面 會員TO 會員)
 * @author user
 */
@Data
public class AdoptionCaseWithApplicationsDTO {

    private AdoptionCaseSummaryDTO caseInfo; //長條摘要
    private List<AdoptionApplicationSummaryDTO> applications; //申請摘要列表 

}
