package com.nanda.analytics.index;

import com.nanda.analytics.domain.entity.IdxSearchDocument;
import com.nanda.analytics.domain.entity.IdxSyncCheckpoint;
import com.nanda.analytics.mapper.IdxSearchDocumentMapper;
import com.nanda.analytics.mapper.IdxSyncCheckpointMapper;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexSyncWorker {

    private final SearchDocumentBuilder searchDocumentBuilder;
    private final IdxSearchDocumentMapper idxSearchDocumentMapper;
    private final IdxSyncCheckpointMapper idxSyncCheckpointMapper;
    private final ElasticsearchIndexClient elasticsearchIndexClient;
    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;

    @Transactional
    public void syncEmpi(Long empiId, Long orgId, String docType) {
        if (empiId == null || orgId == null) {
            return;
        }
        IdxSearchDocument built = searchDocumentBuilder.build(empiId, orgId);
        Long existingId = idxSearchDocumentMapper.findIdByEmpiAndOrg(empiId, orgId);
        built.setUpdatedAt(LocalDateTime.now());
        if (existingId != null) {
            built.setId(existingId);
            idxSearchDocumentMapper.updateById(built);
        } else {
            built.setId(IdGenerator.nextId());
            idxSearchDocumentMapper.insert(built);
        }
        elasticsearchIndexClient.indexDocument(orgId, built);
        updateCheckpoint(orgId, empiId, docType);
        log.info("Index synced empiId={} orgId={} docType={}", empiId, orgId, docType);
    }

    @Transactional
    public int fullRebuild(Long orgId) {
        LambdaQueryWrapper<PubSpecialtyPatient> wrapper = new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getDeleted, 0);
        if (orgId != null) {
            wrapper.eq(PubSpecialtyPatient::getOrgId, orgId);
        }
        java.util.List<PubSpecialtyPatient> patients = pubSpecialtyPatientMapper.selectList(wrapper);
        java.util.Set<String> seen = new java.util.HashSet<String>();
        int count = 0;
        for (PubSpecialtyPatient patient : patients) {
            String key = patient.getOrgId() + ":" + patient.getEmpiId();
            if (!seen.add(key)) {
                continue;
            }
            syncEmpi(patient.getEmpiId(), patient.getOrgId(), "FULL_REBUILD");
            count++;
        }
        if (orgId != null) {
            updateCheckpoint(orgId, null, "FULL_REBUILD");
        }
        return count;
    }

    private void updateCheckpoint(Long orgId, Long empiId, String docType) {
        IdxSyncCheckpoint checkpoint = idxSyncCheckpointMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IdxSyncCheckpoint>()
                        .eq(IdxSyncCheckpoint::getOrgId, orgId)
                        .last("LIMIT 1"));
        if (checkpoint == null) {
            checkpoint = new IdxSyncCheckpoint();
            checkpoint.setId(IdGenerator.nextId());
            checkpoint.setOrgId(orgId);
        }
        checkpoint.setLastSyncAt(LocalDateTime.now());
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("empiId", empiId);
        payload.put("docType", docType);
        checkpoint.setCheckpointJson(JsonUtils.toJson(payload));
        if (idxSyncCheckpointMapper.selectById(checkpoint.getId()) == null) {
            idxSyncCheckpointMapper.insert(checkpoint);
        } else {
            idxSyncCheckpointMapper.updateById(checkpoint);
        }
    }
}
