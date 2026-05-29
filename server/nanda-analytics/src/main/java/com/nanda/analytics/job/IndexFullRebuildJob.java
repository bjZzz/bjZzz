package com.nanda.analytics.job;

import com.nanda.analytics.index.IndexSyncWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexFullRebuildJob {

    private final IndexSyncWorker indexSyncWorker;

    @Scheduled(cron = "0 0 0 * * ?")
    public void rebuildAll() {
        int count = indexSyncWorker.fullRebuild(null);
        log.info("indexFullRebuild completed count={}", count);
    }
}
