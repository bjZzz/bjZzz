package com.nanda.asset.empi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.asset.domain.dto.AssetDtos;
import com.nanda.asset.domain.dto.AssetDtos.EmpiMatchCandidateVO;
import com.nanda.asset.domain.dto.AssetDtos.EmpiMatchRequest;
import com.nanda.asset.domain.dto.AssetDtos.EmpiMatchResultVO;
import com.nanda.asset.domain.dto.AssetDtos.EmpiMatchRuleVO;
import com.nanda.asset.domain.dto.AssetDtos.EmpiPatientVO;
import com.nanda.asset.domain.dto.AssetDtos.TimelineEventVO;
import com.nanda.asset.domain.entity.EmpiMatchCandidate;
import com.nanda.asset.domain.entity.EmpiMatchRule;
import com.nanda.asset.domain.entity.PubSpecialtyLabExam;
import com.nanda.asset.domain.entity.PubSpecialtyMedicalRecord;
import com.nanda.asset.mapper.EmpiMatchCandidateMapper;
import com.nanda.asset.mapper.EmpiMatchRuleMapper;
import com.nanda.asset.mapper.PubSpecialtyLabExamMapper;
import com.nanda.asset.mapper.PubSpecialtyMedicalRecordMapper;
import com.nanda.asset.service.AssetOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.crypto.CryptoService;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.entity.EmpiIdentifier;
import com.nanda.governance.publish.entity.EmpiMaster;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.EmpiIdentifierMapper;
import com.nanda.governance.publish.mapper.EmpiMasterMapper;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmpiMatchService {

    private static final String ID_TYPE_SOURCE = "SOURCE_REF";
    private static final String ID_TYPE_ID_CARD = "ID_CARD";

    private final EmpiMasterMapper empiMasterMapper;
    private final EmpiIdentifierMapper empiIdentifierMapper;
    private final EmpiMatchCandidateMapper empiMatchCandidateMapper;
    private final EmpiMatchRuleMapper empiMatchRuleMapper;
    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;
    private final PubSpecialtyMedicalRecordMapper pubSpecialtyMedicalRecordMapper;
    private final PubSpecialtyLabExamMapper pubSpecialtyLabExamMapper;
    private final MatchScoreCalculator matchScoreCalculator;
    private final CryptoService cryptoService;

    @Transactional
    public EmpiMatchResultVO match(EmpiMatchRequest request) {
        Long orgId = AssetOrgContext.requireOrgId();
        Map<String, Object> config = loadMatchConfig(orgId);
        MatchScoreCalculator.MatchFeatures features = toFeatures(request);

        if (!isBlank(request.getSourceRef())) {
            String hash = cryptoService.hashForIndex(request.getSourceRef(), "empi");
            Long existing = empiMasterMapper.findEmpiIdByHash(ID_TYPE_SOURCE, hash);
            if (existing != null) {
                return matched(existing, "EXACT_HASH", new BigDecimal("1.0000"), null);
            }
        }

        if (!isBlank(request.getIdCard())) {
            String hash = cryptoService.hashForIndex(request.getIdCard(), "empi");
            Long existing = empiMasterMapper.findEmpiIdByHash(ID_TYPE_ID_CARD, hash);
            if (existing != null) {
                BigDecimal crossScore = matchScoreCalculator.crossMatchScore(features);
                if (crossScore.compareTo(matchScoreCalculator.crossThreshold(config)) >= 0) {
                    return matched(existing, "CROSS_MATCH", crossScore, null);
                }
            }
        }

        List<EmpiMaster> candidates = empiMasterMapper.selectList(new LambdaQueryWrapper<EmpiMaster>()
                .eq(EmpiMaster::getOrgId, orgId)
                .eq(EmpiMaster::getDeleted, 0)
                .eq(EmpiMaster::getMergeStatus, "ACTIVE")
                .last("LIMIT 200"));

        EmpiMaster best = null;
        BigDecimal bestScore = BigDecimal.ZERO;
        for (EmpiMaster candidate : candidates) {
            BigDecimal score = matchScoreCalculator.calculate(config, candidate, features);
            if (score.compareTo(bestScore) > 0) {
                bestScore = score;
                best = candidate;
            }
        }

        if (best != null && bestScore.compareTo(matchScoreCalculator.threshold(config)) >= 0) {
            bindIdentifier(best.getId(), request);
            return matched(best.getId(), "FUZZY", bestScore, null);
        }

        if (best != null) {
            EmpiMatchCandidate pending = createCandidate(null, best.getId(), bestScore, features, best);
            return matched(null, "PENDING", bestScore, pending.getId());
        }

        EmpiMaster created = createEmpi(request, orgId, new BigDecimal("1.0000"));
        bindIdentifier(created.getId(), request);
        return matched(created.getId(), "NEW", new BigDecimal("1.0000"), null);
    }

    public PageResult<EmpiMatchCandidateVO> listCandidates(PageQuery query) {
        Page<EmpiMatchCandidate> page = empiMatchCandidateMapper.selectPage(
                new Page<EmpiMatchCandidate>(query.getPage(), query.getSize()),
                new LambdaQueryWrapper<EmpiMatchCandidate>()
                        .eq(EmpiMatchCandidate::getReviewStatus, "PENDING")
                        .orderByDesc(EmpiMatchCandidate::getCreatedAt));
        List<EmpiMatchCandidateVO> items = new ArrayList<EmpiMatchCandidateVO>();
        for (EmpiMatchCandidate candidate : page.getRecords()) {
            items.add(toCandidateVO(candidate));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    @Transactional
    public EmpiMatchResultVO confirmCandidate(Long candidateId) {
        EmpiMatchCandidate candidate = requireCandidate(candidateId);
        candidate.setReviewStatus("CONFIRMED");
        candidate.setReviewerId(AssetOrgContext.currentUserId());
        empiMatchCandidateMapper.updateById(candidate);
        return matched(candidate.getCandidateEmpiId(), "CONFIRMED", candidate.getMatchScore(), candidateId);
    }

    @Transactional
    public EmpiMatchResultVO rejectCandidate(Long candidateId) {
        EmpiMatchCandidate candidate = requireCandidate(candidateId);
        candidate.setReviewStatus("REJECTED");
        candidate.setReviewerId(AssetOrgContext.currentUserId());
        empiMatchCandidateMapper.updateById(candidate);
        EmpiMaster created = createEmpiFromCandidate(candidate);
        return matched(created.getId(), "REJECTED_NEW", new BigDecimal("1.0000"), candidateId);
    }

    public EmpiPatientVO getPatient(Long empiId) {
        EmpiMaster master = requireEmpi(empiId);
        return toPatientVO(master);
    }

    public List<TimelineEventVO> getTimeline(Long empiId) {
        requireEmpi(empiId);
        Long orgId = AssetOrgContext.requireOrgId();
        List<TimelineEventVO> events = new ArrayList<TimelineEventVO>();

        List<PubSpecialtyPatient> patients = pubSpecialtyPatientMapper.selectList(new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getEmpiId, empiId)
                .eq(PubSpecialtyPatient::getOrgId, orgId)
                .eq(PubSpecialtyPatient::getDeleted, 0));
        for (PubSpecialtyPatient patient : patients) {
            TimelineEventVO event = new TimelineEventVO();
            event.setEventType("SPECIALTY_ENROLL");
            event.setTitle("专病入库: " + patient.getSpecialtyType());
            event.setDetail(patient.getCoreFields());
            event.setEventTime(patient.getCreatedAt());
            event.setSourceId(patient.getId());
            events.add(event);

            List<PubSpecialtyMedicalRecord> records = pubSpecialtyMedicalRecordMapper.selectList(
                    new LambdaQueryWrapper<PubSpecialtyMedicalRecord>()
                            .eq(PubSpecialtyMedicalRecord::getPatientId, patient.getId())
                            .eq(PubSpecialtyMedicalRecord::getDeleted, 0));
            for (PubSpecialtyMedicalRecord record : records) {
                TimelineEventVO medical = new TimelineEventVO();
                medical.setEventType("MEDICAL_RECORD");
                medical.setTitle(record.getRecordType());
                medical.setDetail(record.getContentJson());
                medical.setEventTime(record.getCreatedAt());
                medical.setSourceId(record.getId());
                events.add(medical);
            }

            List<PubSpecialtyLabExam> labs = pubSpecialtyLabExamMapper.selectList(new LambdaQueryWrapper<PubSpecialtyLabExam>()
                    .eq(PubSpecialtyLabExam::getPatientId, patient.getId())
                    .eq(PubSpecialtyLabExam::getDeleted, 0));
            for (PubSpecialtyLabExam lab : labs) {
                TimelineEventVO labEvent = new TimelineEventVO();
                labEvent.setEventType("LAB_EXAM");
                labEvent.setTitle(lab.getExamCode());
                labEvent.setDetail(lab.getExamValue() + " " + lab.getExamUnit());
                labEvent.setEventTime(lab.getExamDate() != null
                        ? lab.getExamDate().atStartOfDay()
                        : lab.getCreatedAt());
                labEvent.setSourceId(lab.getId());
                events.add(labEvent);
            }
        }

        events.sort(Comparator.comparing(TimelineEventVO::getEventTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return events;
    }

    public EmpiMatchRuleVO getMatchRule() {
        EmpiMatchRule rule = findActiveRule(AssetOrgContext.requireOrgId());
        EmpiMatchRuleVO vo = new EmpiMatchRuleVO();
        vo.setId(rule.getId());
        vo.setRuleName(rule.getRuleName());
        vo.setRuleConfigJson(rule.getRuleConfigJson());
        vo.setStatus(rule.getStatus());
        return vo;
    }

    @Transactional
    public EmpiMatchRuleVO updateMatchRule(String ruleConfigJson) {
        EmpiMatchRule rule = findActiveRule(AssetOrgContext.requireOrgId());
        rule.setRuleConfigJson(ruleConfigJson);
        empiMatchRuleMapper.updateById(rule);
        return getMatchRule();
    }

    private EmpiMatchCandidate createCandidate(Long sourceRecordId, Long candidateEmpiId, BigDecimal score,
                                               MatchScoreCalculator.MatchFeatures features, EmpiMaster candidate) {
        EmpiMatchCandidate entity = new EmpiMatchCandidate();
        entity.setId(IdGenerator.nextId());
        entity.setSourceRecordId(sourceRecordId);
        entity.setCandidateEmpiId(candidateEmpiId);
        entity.setMatchScore(score);
        entity.setMatchFeatures(JsonUtils.toJson(matchScoreCalculator.buildFeatures(features, candidate, score)));
        entity.setReviewStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        empiMatchCandidateMapper.insert(entity);
        return entity;
    }

    private EmpiMaster createEmpi(EmpiMatchRequest request, Long orgId, BigDecimal confidence) {
        EmpiMaster master = new EmpiMaster();
        master.setId(IdGenerator.nextId());
        master.setDisplayName(request.getName());
        master.setGender(request.getGender());
        master.setBirthDate(parseDate(request.getBirthDate()));
        master.setMergeStatus("ACTIVE");
        master.setMatchConfidence(confidence);
        master.setOrgId(orgId);
        master.setCreatedAt(LocalDateTime.now());
        master.setUpdatedAt(LocalDateTime.now());
        master.setDeleted(0);
        empiMasterMapper.insert(master);
        return master;
    }

    private EmpiMaster createEmpiFromCandidate(EmpiMatchCandidate candidate) {
        EmpiMaster source = empiMasterMapper.selectById(candidate.getCandidateEmpiId());
        EmpiMatchRequest request = new EmpiMatchRequest();
        request.setName(source != null ? source.getDisplayName() : "未知");
        request.setGender(source != null ? source.getGender() : null);
        request.setBirthDate(source != null && source.getBirthDate() != null ? source.getBirthDate().toString() : null);
        return createEmpi(request, AssetOrgContext.requireOrgId(), new BigDecimal("1.0000"));
    }

    private void bindIdentifier(Long empiId, EmpiMatchRequest request) {
        if (!isBlank(request.getSourceRef())) {
            insertIdentifier(empiId, ID_TYPE_SOURCE, request.getSourceRef(), request.getSourceSystem(), 1);
        }
        if (!isBlank(request.getIdCard())) {
            insertIdentifier(empiId, ID_TYPE_ID_CARD, request.getIdCard(), request.getSourceSystem(), 0);
        }
    }

    private void insertIdentifier(Long empiId, String idType, String plainValue, String sourceSystem, int primary) {
        String hash = cryptoService.hashForIndex(plainValue, "empi");
        Long existing = empiMasterMapper.findEmpiIdByHash(idType, hash);
        if (existing != null) {
            return;
        }
        EmpiIdentifier identifier = new EmpiIdentifier();
        identifier.setId(IdGenerator.nextId());
        identifier.setEmpiId(empiId);
        identifier.setIdType(idType);
        identifier.setIdValueEnc(plainValue);
        identifier.setIdHash(hash);
        identifier.setSourceSystem(sourceSystem != null ? sourceSystem : "API");
        identifier.setIsPrimary(primary);
        empiIdentifierMapper.insert(identifier);
    }

    private Map<String, Object> loadMatchConfig(Long orgId) {
        EmpiMatchRule rule = findActiveRule(orgId);
        return matchScoreCalculator.parseConfig(rule.getRuleConfigJson());
    }

    private EmpiMatchRule findActiveRule(Long orgId) {
        EmpiMatchRule rule = empiMatchRuleMapper.selectOne(new LambdaQueryWrapper<EmpiMatchRule>()
                .eq(EmpiMatchRule::getDeleted, 0)
                .eq(EmpiMatchRule::getStatus, "ACTIVE")
                .and(w -> w.eq(EmpiMatchRule::getOrgId, orgId).or().isNull(EmpiMatchRule::getOrgId))
                .last("LIMIT 1"));
        if (rule == null) {
            rule = new EmpiMatchRule();
            rule.setId(0L);
            rule.setRuleName("default");
            rule.setRuleConfigJson(JsonUtils.toJson(matchScoreCalculator.defaultConfig()));
            rule.setStatus("ACTIVE");
        }
        return rule;
    }

    private EmpiMatchCandidate requireCandidate(Long id) {
        EmpiMatchCandidate candidate = empiMatchCandidateMapper.selectById(id);
        if (candidate == null || !"PENDING".equals(candidate.getReviewStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "匹配候选不存在或已处理");
        }
        return candidate;
    }

    private EmpiMaster requireEmpi(Long empiId) {
        EmpiMaster master = empiMasterMapper.selectById(empiId);
        if (master == null || master.getDeleted() != null && master.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "EMPI 患者不存在");
        }
        if (!master.getOrgId().equals(AssetOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该患者");
        }
        return master;
    }

    private EmpiMatchResultVO matched(Long empiId, String matchType, BigDecimal confidence, Long candidateId) {
        EmpiMatchResultVO vo = new EmpiMatchResultVO();
        vo.setEmpiId(empiId);
        vo.setMatchType(matchType);
        vo.setConfidence(confidence);
        vo.setCandidateId(candidateId);
        return vo;
    }

    private EmpiMatchCandidateVO toCandidateVO(EmpiMatchCandidate candidate) {
        EmpiMatchCandidateVO vo = new EmpiMatchCandidateVO();
        vo.setId(candidate.getId());
        vo.setCandidateEmpiId(candidate.getCandidateEmpiId());
        EmpiMaster master = empiMasterMapper.selectById(candidate.getCandidateEmpiId());
        vo.setCandidateName(master != null ? master.getDisplayName() : null);
        vo.setMatchScore(candidate.getMatchScore());
        vo.setMatchFeatures(candidate.getMatchFeatures());
        vo.setReviewStatus(candidate.getReviewStatus());
        vo.setCreatedAt(candidate.getCreatedAt());
        return vo;
    }

    private EmpiPatientVO toPatientVO(EmpiMaster master) {
        EmpiPatientVO vo = new EmpiPatientVO();
        vo.setId(master.getId());
        vo.setDisplayName(master.getDisplayName());
        vo.setGender(master.getGender());
        vo.setBirthDate(master.getBirthDate());
        vo.setMergeStatus(master.getMergeStatus());
        vo.setMatchConfidence(master.getMatchConfidence());
        return vo;
    }

    private MatchScoreCalculator.MatchFeatures toFeatures(EmpiMatchRequest request) {
        return new MatchScoreCalculator.MatchFeatures(
                request.getName(), request.getPhone(), request.getAddress(),
                request.getBirthDate(), request.getIdCard());
    }

    private LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
