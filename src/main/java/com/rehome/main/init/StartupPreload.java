package com.rehome.main.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rehome.main.service.BannerService;
import com.rehome.main.service.CaseService;

@Component
public class StartupPreload implements CommandLineRunner {

    @Autowired
    private CaseService caseService;
    @Autowired
    private BannerService bannerService;

    @Override
    public void run(String... args) throws Exception {
        // 預熱方法，讓後續查找資料快速
        caseService.getHomeInfo(null, true);
        caseService.getOptions();

        bannerService.getActiveBanners();
    }
}
