package com.nanda.asset.specialty;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.asset.domain.dto.AssetDtos.CockpitSummaryVO;
import com.nanda.asset.domain.dto.AssetDtos.Patient360VO;
import com.nanda.asset.domain.dto.AssetDtos.SpecialtyOverviewVO;
import com.nanda.asset.domain.dto.AssetDtos.SpecialtyPatientVO;
import com.nanda.asset.domain.dto.AssetDtos.TimelineEventVO;
import com.nanda.asset.domain.entity.EmpiMatchCandidate;
import com.nanda.asset.domain.entity.PubComorbidityView;
import com.nanda.asset.domain.entity.PubSpecialtyFollowUp;
import com.nanda.asset.domain.entity.PubSpecialtyLabExam;
import com.nanda.asset.domain.entity.PubSpecialtyMedicalRecord;
import com.nanda.asset.domain.entity.PubSpecialtyTreatment;
import com.nanda.asset.domain.entity.QcReviewTask;
import com.nanda.asset.domain.enums.SpecialtyType;
import com.nanda.asset.empi.EmpiMatchService;
import com.nanda.asset.mapper.EmpiMatchCandidateMapper;
import com.nanda.asset.mapper.PubComorbidityViewMapper;
import com.nanda.asset.mapper.PubSpecialtyFollowUpMapper;
import com.nanda.asset.mapper.PubSpecialtyLabExamMapper;
import com.nanda.asset.mapper.PubSpecialtyMedicalRecordMapper;
import com.nanda.asset.mapper.PubSpecialtyTreatmentMapper;
import com.nanda.asset.mapper.QcReviewTaskMapper;
import com.nanda.asset.service.AssetOrgContext;
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
public class Patient360Service {

    private final SpecialtyPatientService specialtyPatientService;
    private final EmpiMatchService empiMatchService;
    private final EmpiMasterMapper empiMasterMapper;
    private final PubSpecialtyMedicalRecordMapper pubSpecialtyMedicalRecordMapper;
    private final PubSpecialtyLabExamMapper pubSpecialtyLabExamMapper;
    private final PubSpecialtyTreatmentMapper pubSpecialtyTreatmentMapper;
    private final PubSpecialtyFollowUpMapper pubSpecialtyFollowUpMapper;

    public Patient360VO build360(SpecialtyType type, Long recordId) {
        PubSpecialtyPatient patient = specialtyPatientService.requirePatient(type, recordId);
        Patient360VO vo = new Patient360VO();
        vo.setPatient(specialtyPatientService.getPatient(type, recordId));
        EmpiMaster empi = empiMasterMapper.selectById(patient.getEmpiId());
        if (empi != null) {
            vo.setEmpi(empiMatchService.getPatient(empi.getId()));
        }
        vo.setTimeline(empiMatchService.getTimeline(patient.getEmpiId()));
        vo.setMedicalRecords(loadMedicalRecords(patient.getId()));
        vo.setLabResults(loadLabResults(patient.getId()));
        vo.setTreatments(loadTreatments(patient.getId()));
        vo.setFollowUps(loadFollowUps(patient.getId()));
        return vo;
    }

    public List<TimelineEventVO> getTimeline(SpecialtyType type, Long recordId) {
        PubSpecialtyPatient patient = specialtyPatientService.requirePatient(type, recordId);
        return empiMatchService.getTimeline(patient.getEmpiId());
    }

    private List<Map<String, Object>> loadMedicalRecords(Long patientId) {
        List<PubSpecialtyMedicalRecord> records = pubSpecialtyMedicalRecordMapper.selectList(
                new LambdaQueryWrapper<PubSpecialtyMedicalRecord>()
                        .eq(PubSpecialtyMedicalRecord::getPatientId, patientId)
                        .eq(PubSpecialtyMedicalRecord::getDeleted, 0));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (PubSpecialtyMedicalRecord record : records) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", record.getId());
            item.put("recordType", record.getRecordType());
            item.put("content", JsonUtils.fromJson(record.getContentJson(), new TypeReference<Map<String, Object>>() {
            }));
            item.put("createdAt", record.getCreatedAt());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> loadLabResults(Long patientId) {
        List<PubSpecialtyLabExam> labs = pubSpecialtyLabExamMapper.selectList(new LambdaQueryWrapper<PubSpecialtyLabExam>()
                .eq(PubSpecialtyLabExam::getPatientId, patientId)
                .eq(PubSpecialtyLabExam::getDeleted, 0));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (PubSpecialtyLabExam lab : labs) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", lab.getId());
            item.put("examCode", lab.getExamCode());
            item.put("examValue", lab.getExamValue());
            item.put("examUnit", lab.getExamUnit());
            item.put("examDate", lab.getExamDate());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> loadTreatments(Long patientId) {
        List<PubSpecialtyTreatment> treatments = pubSpecialtyTreatmentMapper.selectList(
                new LambdaQueryWrapper<PubSpecialtyTreatment>()
                        .eq(PubSpecialtyTreatment::getPatientId, patientId)
                        .eq(PubSpecialtyTreatment::getDeleted, 0));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (PubSpecialtyTreatment treatment : treatments) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", treatment.getId());
            item.put("treatment", JsonUtils.fromJson(treatment.getTreatmentJson(), new TypeReference<Map<String, Object>>() {
            }));
            item.put("createdAt", treatment.getCreatedAt());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> loadFollowUps(Long patientId) {
        List<PubSpecialtyFollowUp> followUps = pubSpecialtyFollowUpMapper.selectList(
                new LambdaQueryWrapper<PubSpecialtyFollowUp>()
                        .eq(PubSpecialtyFollowUp::getPatientId, patientId)
                        .eq(PubSpecialtyFollowUp::getDeleted, 0));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (PubSpecialtyFollowUp followUp : followUps) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", followUp.getId());
            item.put("followUp", JsonUtils.fromJson(followUp.getFollowUpJson(), new TypeReference<Map<String, Object>>() {
            }));
            item.put("createdAt", followUp.getCreatedAt());
            result.add(item);
        }
        return result;
    }
}
