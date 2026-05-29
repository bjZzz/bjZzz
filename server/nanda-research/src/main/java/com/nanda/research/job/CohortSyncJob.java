package com.nanda.research.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.research.cohort.CohortService;
import com.nanda.research.domain.entity.ResCohort;
import com.nanda.research.followup.FollowUpTaskService;
import com.nanda.research.mapper.ResCohortMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CohortSyncJob {

    private final ResCohortMapper resCohortMapper;
    private final CohortService cohortService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void syncDynamicCohorts() {
        List<ResCohort> cohorts = resCohortMapper.selectList(new LambdaQueryWrapper<ResCohort>()
                .eq(ResCohort::getDeleted, 0)
                .isNotNull(ResCohort::getRuleJson));
        int totalChanged = 0;
        for (ResCohort cohort : cohorts) {
            totalChanged += cohortService.syncDynamicMembers(cohort.getId());
        }
        log.info("cohortDynamicSync completed cohorts={} changed={}", cohorts.size(), totalChanged);
    }
}
