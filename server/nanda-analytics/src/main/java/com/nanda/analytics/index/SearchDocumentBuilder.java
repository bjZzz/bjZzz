package com.nanda.analytics.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.analytics.domain.entity.IdxSearchDocument;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.entity.EmpiMaster;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.EmpiMasterMapper;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.asset.domain.entity.PubSpecialtyLabExam;
import com.nanda.asset.mapper.PubSpecialtyLabExamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SearchDocumentBuilder {

    private final EmpiMasterMapper empiMasterMapper;
    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;
    private final PubSpecialtyLabExamMapper pubSpecialtyLabExamMapper;

    public IdxSearchDocument build(Long empiId, Long orgId) {
        EmpiMaster empi = empiMasterMapper.selectById(empiId);
        List<PubSpecialtyPatient> patients = pubSpecialtyPatientMapper.selectList(new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getEmpiId, empiId)
                .eq(PubSpecialtyPatient::getOrgId, orgId)
                .eq(PubSpecialtyPatient::getDeleted, 0));

        Set<String> specialtyTypes = new HashSet<String>();
        Set<String> diagnosisCodes = new HashSet<String>();
        List<Map<String, Object>> labValues = new ArrayList<Map<String, Object>>();
        int completeFields = 0;
        int totalFields = 0;

        for (PubSpecialtyPatient patient : patients) {
            specialtyTypes.add(patient.getSpecialtyType());
            Map<String, Object> core = JsonUtils.fromJson(patient.getCoreFields(), new TypeReference<Map<String, Object>>() {
            });
            if (core != null) {
                Object diagnosis = core.get("diagnosisCode");
                if (diagnosis != null) {
                    diagnosisCodes.add(String.valueOf(diagnosis));
                }
                Object name = core.get("name");
                totalFields++;
                if (name != null && !String.valueOf(name).isEmpty()) {
                    completeFields++;
                }
            }
            List<PubSpecialtyLabExam> labs = pubSpecialtyLabExamMapper.selectList(new LambdaQueryWrapper<PubSpecialtyLabExam>()
                    .eq(PubSpecialtyLabExam::getPatientId, patient.getId())
                    .eq(PubSpecialtyLabExam::getDeleted, 0));
            for (PubSpecialtyLabExam lab : labs) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("examCode", lab.getExamCode());
                item.put("examValue", lab.getExamValue());
                item.put("examUnit", lab.getExamUnit());
                item.put("examDate", lab.getExamDate());
                labValues.add(item);
            }
        }

        Map<String, Object> demographics = new HashMap<String, Object>();
        if (empi != null) {
            demographics.put("displayName", empi.getDisplayName());
            demographics.put("gender", empi.getGender());
            demographics.put("birthDate", empi.getBirthDate());
        }

        BigDecimal completeness = totalFields == 0
                ? BigDecimal.ONE
                : BigDecimal.valueOf(completeFields).divide(BigDecimal.valueOf(totalFields), 2, RoundingMode.HALF_UP);

        IdxSearchDocument document = new IdxSearchDocument();
        document.setEmpiId(empiId);
        document.setOrgId(orgId);
        document.setSpecialtyTypes(JsonUtils.toJson(new ArrayList<String>(specialtyTypes)));
        document.setDiagnosisCodes(JsonUtils.toJson(new ArrayList<String>(diagnosisCodes)));
        document.setLabValues(JsonUtils.toJson(labValues));
        document.setDemographics(JsonUtils.toJson(demographics));
        document.setCompletenessScore(completeness);
        return document;
    }
}
