package com.nanda.asset.specialty;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.asset.domain.dto.AssetDtos.CockpitSummaryVO;
import com.nanda.asset.domain.dto.AssetDtos.SpecialtyOverviewVO;
import com.nanda.asset.domain.entity.EmpiMatchCandidate;
import com.nanda.asset.domain.entity.QcReviewTask;
import com.nanda.asset.domain.enums.SpecialtyType;
import com.nanda.asset.mapper.EmpiMatchCandidateMapper;
import com.nanda.asset.mapper.PubComorbidityViewMapper;
import com.nanda.asset.mapper.QcReviewTaskMapper;
import com.nanda.asset.service.AssetOrgContext;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialtyOverviewService {

    private final SpecialtyPatientService specialtyPatientService;
    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;
    private final EmpiMatchCandidateMapper empiMatchCandidateMapper;
    private final QcReviewTaskMapper qcReviewTaskMapper;
    private final PubComorbidityViewMapper pubComorbidityViewMapper;

    public CockpitSummaryVO cockpitSummary() {
        Long orgId = AssetOrgContext.requireOrgId();
        CockpitSummaryVO summary = new CockpitSummaryVO();
        summary.setTotalPatients(pubSpecialtyPatientMapper.selectCount(new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getOrgId, orgId)
                .eq(PubSpecialtyPatient::getDeleted, 0)));
        summary.setPendingMatchCandidates(empiMatchCandidateMapper.selectCount(new LambdaQueryWrapper<EmpiMatchCandidate>()
                .eq(EmpiMatchCandidate::getReviewStatus, "PENDING")));
        summary.setOpenReviewTasks(qcReviewTaskMapper.selectCount(new LambdaQueryWrapper<QcReviewTask>()
                .eq(QcReviewTask::getOrgId, orgId)
                .in(QcReviewTask::getStatus, "OPEN", "IN_REVIEW")));
        summary.setComorbidityViews(pubComorbidityViewMapper.selectCount(null));

        List<SpecialtyOverviewVO> overviews = new ArrayList<SpecialtyOverviewVO>();
        for (SpecialtyType type : SpecialtyType.values()) {
            overviews.add(specialtyPatientService.overview(type));
        }
        summary.setSpecialtyOverviews(overviews);
        return summary;
    }
}
