package com.rehome.main.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.rehome.main.entity.Case;
import com.rehome.main.repository.CaseRepository;
import com.rehome.main.repository.CaseStatusRepo;
import com.rehome.main.repository.CaseTypeRepo;
import com.rehome.main.repository.MemberRepository;

@Service
public class CaseAtomService {
@Autowired
    private CaseRepository caseRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CaseStatusRepo caseStatusRepository;
    @Autowired
    private CaseTypeRepo caseTypeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSingleCase(Case newCase) {
        // 1. 重新獲取 Member (這會讓 Member 進入目前的 Transaction)
        if (newCase.getMember() != null) {
            newCase.setMember(memberRepository.findById(newCase.getMember().getId()).orElse(null));
        }

        // 2. 重新獲取 CaseStatus
        if (newCase.getCaseStatus() != null) {
            newCase.setCaseStatus(caseStatusRepository.findById(newCase.getCaseStatus().getId()).orElse(null));
        }

        // 3. 重新獲取 CaseType
        if (newCase.getCaseType() != null) {
            newCase.setCaseType(caseTypeRepository.findById(newCase.getCaseType().getId()).orElse(null));
        }

        // 現在 newCase 裡面的所有關聯物件都是 Managed 狀態了
        caseRepository.saveAndFlush(newCase); 
    }
}
