package com.nanda.asset.supplement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.asset.domain.dto.AssetDtos.DualScreenSupplementRequest;
import com.nanda.asset.domain.dto.AssetDtos.SupplementResultVO;
import com.nanda.asset.domain.entity.PubDataChangeLog;
import com.nanda.asset.mapper.PubDataChangeLogMapper;
import com.nanda.asset.service.AssetOrgContext;
import com.nanda.common.audit.AuditLog;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupplementService {

    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;
    private final PubDataChangeLogMapper pubDataChangeLogMapper;

    @Transactional
    @AuditLog(action = "SUPPLEMENT", resourceType = "PUB_SPECIALTY_PATIENT")
    public SupplementResultVO dualScreenSave(DualScreenSupplementRequest request) {
        PubSpecialtyPatient patient = pubSpecialtyPatientMapper.selectById(request.getPatientId());
        if (patient == null || patient.getDeleted() != null && patient.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "专病记录不存在");
        }
        if (!patient.getOrgId().equals(AssetOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权补录该记录");
        }
        if (request.getFieldCode() == null || request.getFieldCode().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "字段编码不能为空");
        }

        Map<String, Object> before = JsonUtils.fromJson(patient.getCoreFields(), new TypeReference<Map<String, Object>>() {
        });
        if (before == null) {
            before = new HashMap<String, Object>();
        }
        Map<String, Object> after = new HashMap<String, Object>(before);
        after.put(request.getFieldCode(), request.getFieldValue());

        patient.setCoreFields(JsonUtils.toJson(after));
        patient.setUpdatedBy(AssetOrgContext.currentUserId());
        patient.setUpdatedAt(LocalDateTime.now());
        pubSpecialtyPatientMapper.updateById(patient);

        PubDataChangeLog changeLog = new PubDataChangeLog();
        changeLog.setId(IdGenerator.nextId());
        changeLog.setPatientId(patient.getId());
        changeLog.setChangeType("DUAL_SCREEN_SUPPLEMENT");
        changeLog.setBeforeJson(JsonUtils.toJson(before));
        changeLog.setAfterJson(JsonUtils.toJson(after));
        changeLog.setOperatorId(AssetOrgContext.currentUserId());
        changeLog.setOrgId(patient.getOrgId());
        changeLog.setCreatedAt(LocalDateTime.now());
        pubDataChangeLogMapper.insert(changeLog);

        SupplementResultVO vo = new SupplementResultVO();
        vo.setChangeLogId(changeLog.getId());
        vo.setPatientId(patient.getId());
        vo.setFieldCode(request.getFieldCode());
        vo.setStatus("SAVED");
        return vo;
    }
}
