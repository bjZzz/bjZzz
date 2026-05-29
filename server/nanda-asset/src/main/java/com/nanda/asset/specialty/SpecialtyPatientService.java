package com.nanda.asset.specialty;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.asset.domain.dto.AssetDtos.Patient360VO;
import com.nanda.asset.domain.dto.AssetDtos.SpecialtyOverviewVO;
import com.nanda.asset.domain.dto.AssetDtos.SpecialtyPatientVO;
import com.nanda.asset.domain.dto.AssetDtos.TimelineEventVO;
import com.nanda.asset.domain.entity.PubSpecialtyFollowUp;
import com.nanda.asset.domain.entity.PubSpecialtyLabExam;
import com.nanda.asset.domain.entity.PubSpecialtyMedicalRecord;
import com.nanda.asset.domain.entity.PubSpecialtyTreatment;
import com.nanda.asset.domain.enums.SpecialtyType;
import com.nanda.asset.empi.EmpiMatchService;
import com.nanda.asset.mapper.PubSpecialtyFollowUpMapper;
import com.nanda.asset.mapper.PubSpecialtyLabExamMapper;
import com.nanda.asset.mapper.PubSpecialtyMedicalRecordMapper;
import com.nanda.asset.mapper.PubSpecialtyTreatmentMapper;
import com.nanda.asset.service.AssetOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.entity.EmpiMaster;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.EmpiMasterMapper;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpecialtyPatientService {

    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;
    private final EmpiMasterMapper empiMasterMapper;

    public PageResult<SpecialtyPatientVO> listPatients(SpecialtyType type, PageQuery query) {
        Long orgId = AssetOrgContext.requireOrgId();
        Page<PubSpecialtyPatient> page = pubSpecialtyPatientMapper.selectPage(
                new Page<PubSpecialtyPatient>(query.getPage(), query.getSize()),
                new LambdaQueryWrapper<PubSpecialtyPatient>()
                        .eq(PubSpecialtyPatient::getOrgId, orgId)
                        .eq(PubSpecialtyPatient::getSpecialtyType, type.name())
                        .eq(PubSpecialtyPatient::getDeleted, 0)
                        .orderByDesc(PubSpecialtyPatient::getCreatedAt));
        List<SpecialtyPatientVO> items = new ArrayList<SpecialtyPatientVO>();
        for (PubSpecialtyPatient patient : page.getRecords()) {
            items.add(toVO(patient));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    public SpecialtyPatientVO getPatient(SpecialtyType type, Long recordId) {
        PubSpecialtyPatient patient = requirePatient(type, recordId);
        return toVO(patient);
    }

    public SpecialtyOverviewVO overview(SpecialtyType type) {
        Long orgId = AssetOrgContext.requireOrgId();
        long total = pubSpecialtyPatientMapper.selectCount(new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getOrgId, orgId)
                .eq(PubSpecialtyPatient::getSpecialtyType, type.name())
                .eq(PubSpecialtyPatient::getDeleted, 0));
        long active = pubSpecialtyPatientMapper.selectCount(new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getOrgId, orgId)
                .eq(PubSpecialtyPatient::getSpecialtyType, type.name())
                .eq(PubSpecialtyPatient::getStatus, "ACTIVE")
                .eq(PubSpecialtyPatient::getDeleted, 0));
        SpecialtyOverviewVO vo = new SpecialtyOverviewVO();
        vo.setSpecialtyType(type.name());
        vo.setTotalPatients(total);
        vo.setActivePatients(active);
        vo.setPendingCandidates(0);
        return vo;
    }

    public PubSpecialtyPatient requirePatient(SpecialtyType type, Long recordId) {
        PubSpecialtyPatient patient = pubSpecialtyPatientMapper.selectById(recordId);
        if (patient == null || patient.getDeleted() != null && patient.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "专病记录不存在");
        }
        if (!patient.getOrgId().equals(AssetOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该专病记录");
        }
        if (!type.name().equals(patient.getSpecialtyType())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "专病类型不匹配");
        }
        return patient;
    }

    private SpecialtyPatientVO toVO(PubSpecialtyPatient patient) {
        SpecialtyPatientVO vo = new SpecialtyPatientVO();
        vo.setId(patient.getId());
        vo.setEmpiId(patient.getEmpiId());
        vo.setSpecialtyType(patient.getSpecialtyType());
        vo.setStatus(patient.getStatus());
        vo.setCoreFields(patient.getCoreFields());
        vo.setExtendedFields(patient.getExtendedFields());
        vo.setFirstDiagnosisDate(patient.getFirstDiagnosisDate());
        vo.setCreatedAt(patient.getCreatedAt());
        EmpiMaster empi = empiMasterMapper.selectById(patient.getEmpiId());
        vo.setDisplayName(empi != null ? empi.getDisplayName() : null);
        return vo;
    }
}
