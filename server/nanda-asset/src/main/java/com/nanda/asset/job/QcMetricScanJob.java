package com.nanda.asset.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QcMetricScanJob {

    @Scheduled(cron = "0 0 2 * * ?")
    public void scanMetrics() {
        log.info("QcMetricScanJob skipped in batch mode — dashboard computes metrics on demand");
    }
}
