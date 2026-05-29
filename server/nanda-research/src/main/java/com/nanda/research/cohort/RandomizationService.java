package com.nanda.research.cohort;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import com.nanda.research.domain.entity.ResCohortMember;
import com.nanda.research.domain.entity.ResRandomizationRecord;
import com.nanda.research.mapper.ResCohortMemberMapper;
import com.nanda.research.mapper.ResRandomizationRecordMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RandomizationService {

    private final ResCohortMemberMapper resCohortMemberMapper;
    private final ResRandomizationRecordMapper resRandomizationRecordMapper;
    private final CohortService cohortService;

    @Transactional
    public RandomizeResult randomize(Long cohortId, RandomizeRequest request) {
        cohortService.requireCohort(cohortId);
        List<ResCohortMember> members = resCohortMemberMapper.selectList(new LambdaQueryWrapper<ResCohortMember>()
                .eq(ResCohortMember::getCohortId, cohortId)
                .eq(ResCohortMember::getStatus, "ACTIVE")
                .isNull(ResCohortMember::getGroupLabel));
        if (members.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE, "没有待随机分组的成员");
        }

        List<String> groups = resolveGroups(request);
        long seed = request.getSeed() != null ? request.getSeed() : System.currentTimeMillis();
        Collections.shuffle(members, new Random(seed));

        int assigned = 0;
        for (int i = 0; i < members.size(); i++) {
            ResCohortMember member = members.get(i);
            String group = groups.get(i % groups.size());
            member.setGroupLabel(group);
            resCohortMemberMapper.updateById(member);

            ResRandomizationRecord record = new ResRandomizationRecord();
            record.setId(IdGenerator.nextId());
            record.setCohortId(cohortId);
            record.setCohortMemberId(member.getId());
            record.setGroupAssigned(group);
            record.setRandomizedAt(LocalDateTime.now());
            resRandomizationRecordMapper.insert(record);
            assigned++;
        }

        RandomizeResult result = new RandomizeResult();
        result.setAssigned(assigned);
        result.setSeed(seed);
        return result;
    }

    private List<String> resolveGroups(RandomizeRequest request) {
        List<String> groups = new ArrayList<String>();
        if (request.getGroups() != null && !request.getGroups().isEmpty()) {
            groups.addAll(request.getGroups());
            return groups;
        }
        groups.add("A");
        groups.add("B");
        return groups;
    }

    @Data
    public static class RandomizeRequest {
        private String algorithm;
        private List<String> groups;
        private Long seed;
    }

    @Data
    public static class RandomizeResult {
        private int assigned;
        private long seed;
    }
}
