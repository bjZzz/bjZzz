package com.nanda.asset.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.asset.domain.entity.EmpiMatchCandidate;
import com.nanda.asset.mapper.EmpiMatchCandidateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmpiCandidateExpireJob {

    private static final int EXPIRE_DAYS = 30;

    private final EmpiMatchCandidateMapper empiMatchCandidateMapper;

    @Scheduled(cron = "0 0 4 * * ?")
    public void expireCandidates() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(EXPIRE_DAYS);
        int removed = empiMatchCandidateMapper.delete(new LambdaQueryWrapper<EmpiMatchCandidate>()
                .eq(EmpiMatchCandidate::getReviewStatus, "PENDING")
                .lt(EmpiMatchCandidate::getCreatedAt, threshold));
        log.info("empiCandidateExpire removed={}", removed);
    }
}
