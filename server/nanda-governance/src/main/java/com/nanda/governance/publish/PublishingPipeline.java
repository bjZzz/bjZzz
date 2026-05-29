package com.nanda.governance.publish;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.event.CleaningCompletedEvent;
import com.nanda.common.event.DataPublishedEvent;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.cleaning.CleanedRecord;
import com.nanda.governance.cleaning.CleaningEngine;
import com.nanda.governance.domain.entity.GovPublishRule;
import com.nanda.governance.domain.entity.GovPublishTask;
import com.nanda.governance.mapper.GovPublishRuleMapper;
import com.nanda.governance.mapper.GovPublishTaskMapper;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import com.nanda.ingestion.domain.entity.StgBatch;
import com.nanda.ingestion.domain.entity.StgRecord;
import com.nanda.ingestion.domain.enums.BatchStatus;
import com.nanda.ingestion.mapper.StgBatchMapper;
import com.nanda.ingestion.mapper.StgRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublishingPipeline {

    private final StgBatchMapper stgBatchMapper;
    private final StgRecordMapper stgRecordMapper;
    private final CleaningEngine cleaningEngine;
    private final SimpleEmpiMatcher empiMatcher;
    private final GovPublishRuleMapper govPublishRuleMapper;
    private final GovPublishTaskMapper govPublishTaskMapper;
    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;
    private final PublishRuleEvaluator publishRuleEvaluator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processBatch(Long batchId) {
        StgBatch batch = stgBatchMapper.selectById(batchId);
        if (batch == null) {
            log.warn("Batch not found batchId={}", batchId);
            return;
        }
        updateBatchStatus(batch, BatchStatus.CLEANING);

        List<StgRecord> records = stgRecordMapper.selectList(new LambdaQueryWrapper<StgRecord>()
                .eq(StgRecord::getBatchId, batchId)
                .eq(StgRecord::getParseStatus, "OK"));
        List<CleanedRecord> cleaned = cleaningEngine.clean(batchId, batch.getOrgId(), records);
        eventPublisher.publishEvent(new CleaningCompletedEvent(this, batchId, batch.getOrgId(), cleaned.size()));

        updateBatchStatus(batch, BatchStatus.MATCHED);
        int published = 0;
        for (CleanedRecord cr : cleaned) {
            empiMatcher.match(cr, batch.getOrgId());
        }
        updateBatchStatus(batch, BatchStatus.READY_TO_PUBLISH);

        List<GovPublishRule> rules = govPublishRuleMapper.selectList(new LambdaQueryWrapper<GovPublishRule>()
                .eq(GovPublishRule::getDeleted, 0)
                .and(w -> w.eq(GovPublishRule::getOrgId, batch.getOrgId()).or().isNull(GovPublishRule::getOrgId)));
        if (rules.isEmpty()) {
            GovPublishRule defaultRule = defaultRule(batch.getOrgId());
            rules = java.util.Collections.singletonList(defaultRule);
        }

        GovPublishTask task = new GovPublishTask();
        task.setId(IdGenerator.nextId());
        task.setBatchId(batchId);
        task.setStatus("RUNNING");
        task.setOrgId(batch.getOrgId());
        task.setCreatedAt(LocalDateTime.now());
        govPublishTaskMapper.insert(task);

        for (CleanedRecord cr : cleaned) {
            for (GovPublishRule rule : rules) {
                if (!publishRuleEvaluator.evaluate(rule, cr)) {
                    continue;
                }
                PubSpecialtyPatient patient = new PubSpecialtyPatient();
                patient.setId(IdGenerator.nextId());
                patient.setEmpiId(cr.getEmpiId());
                patient.setSpecialtyType(rule.getSpecialtyType());
                patient.setOrgId(batch.getOrgId());
                patient.setCoreFields(JsonUtils.toJson(cr.getPayload()));
                patient.setExtendedFields("{}");
                patient.setStatus("ACTIVE");
                patient.setCreatedAt(LocalDateTime.now());
                patient.setUpdatedAt(LocalDateTime.now());
                patient.setDeleted(0);
                pubSpecialtyPatientMapper.insert(patient);
                published++;
                eventPublisher.publishEvent(new DataPublishedEvent(
                        this, cr.getEmpiId(), rule.getSpecialtyType(), patient.getId(), batch.getOrgId()));
            }
        }

        task.setStatus(published > 0 ? "COMPLETED" : "FAILED");
        govPublishTaskMapper.updateById(task);

        if (published > 0) {
            batch.setSuccessCount(published);
            updateBatchStatus(batch, BatchStatus.PUBLISHED);
        } else {
            batch.setErrorMessage("No records published");
            updateBatchStatus(batch, BatchStatus.REJECTED);
        }
        log.info("PublishingPipeline batchId={} published={}", batchId, published);
    }

    private GovPublishRule defaultRule(Long orgId) {
        GovPublishRule rule = new GovPublishRule();
        rule.setSpecialtyType("METABOLIC");
        rule.setInclusionJson("{\"domainEquals\":\"PATIENT\"}");
        rule.setOrgId(orgId);
        return rule;
    }

    private void updateBatchStatus(StgBatch batch, String status) {
        batch.setStatus(status);
        stgBatchMapper.updateById(batch);
    }
}
