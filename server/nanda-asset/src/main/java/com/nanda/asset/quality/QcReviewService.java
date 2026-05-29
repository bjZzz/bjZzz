package com.nanda.asset.quality;

import com.nanda.asset.domain.dto.AssetDtos.QcReviewRequest;
import com.nanda.asset.domain.dto.AssetDtos.QcReviewTaskVO;
import com.nanda.asset.domain.entity.QcReviewTask;
import com.nanda.asset.domain.entity.QcSampleRecord;
import com.nanda.asset.mapper.QcReviewTaskMapper;
import com.nanda.asset.mapper.QcSampleRecordMapper;
import com.nanda.asset.service.AssetOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QcReviewService {

    private final QcReviewTaskMapper qcReviewTaskMapper;
    private final QcSampleRecordMapper qcSampleRecordMapper;
    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;

    @Transactional
    public QcReviewTaskVO review(Long taskId, QcReviewRequest request) {
        QcReviewTask task = qcReviewTaskMapper.selectById(taskId);
        if (task == null || !task.getOrgId().equals(AssetOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "复核任务不存在");
        }
        if ("APPROVE".equalsIgnoreCase(request.getDecision())) {
            task.setStatus("CLOSED");
        } else if ("REJECT".equalsIgnoreCase(request.getDecision())) {
            task.setStatus("IN_REVIEW");
        } else {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "不支持的审核决策");
        }
        task.setReviewerId(AssetOrgContext.currentUserId());
        qcReviewTaskMapper.updateById(task);

        QcReviewTaskVO vo = new QcReviewTaskVO();
        vo.setId(task.getId());
        vo.setSampleRecordId(task.getSampleRecordId());
        QcSampleRecord record = qcSampleRecordMapper.selectById(task.getSampleRecordId());
        vo.setPatientId(record != null ? record.getPatientId() : null);
        vo.setStatus(task.getStatus());
        vo.setCreatedAt(task.getCreatedAt());
        return vo;
    }

    public Map<String, Object> compare(Long patientId) {
        PubSpecialtyPatient patient = pubSpecialtyPatientMapper.selectById(patientId);
        if (patient == null || !patient.getOrgId().equals(AssetOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "患者不存在");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("patientId", patientId);
        result.put("published", patient.getCoreFields());
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("name", "源系统快照");
        source.put("note", "W4 前为占位比对");
        result.put("source", source);
        result.put("diffFields", new ArrayList<String>());
        return result;
    }
}
