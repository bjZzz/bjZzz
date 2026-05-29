package com.nanda.analytics.sandbox;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.AlgorithmRegisterRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.AlgorithmVO;
import com.nanda.analytics.domain.entity.AnaAlgorithmRegistry;
import com.nanda.analytics.mapper.AnaAlgorithmRegistryMapper;
import com.nanda.analytics.service.AnalyticsOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlgorithmRegistryService {

    private final AnaAlgorithmRegistryMapper algorithmRegistryMapper;
    private final SandboxClient sandboxClient;

    public List<AlgorithmVO> list() {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        Long userId = AnalyticsOrgContext.currentUserId();
        List<AlgorithmVO> result = new ArrayList<AlgorithmVO>();
        List<AnaAlgorithmRegistry> local = algorithmRegistryMapper.selectList(
                new LambdaQueryWrapper<AnaAlgorithmRegistry>()
                        .eq(AnaAlgorithmRegistry::getStatus, "ACTIVE")
                        .orderByAsc(AnaAlgorithmRegistry::getAlgorithmCode));
        for (AnaAlgorithmRegistry row : local) {
            result.add(toVO(row));
        }
        for (Map<String, Object> remote : sandboxClient.listRemoteAlgorithms(userId, orgId)) {
            AlgorithmVO vo = new AlgorithmVO();
            vo.setAlgorithmCode(stringVal(remote.get("algorithmCode")));
            vo.setAlgorithmName(stringVal(remote.get("algorithmName")));
            vo.setVersion(stringVal(remote.get("version")));
            vo.setPackageRef(stringVal(remote.get("packageRef")));
            vo.setStatus("ACTIVE");
            result.add(vo);
        }
        return result;
    }

    @Transactional
    public AlgorithmVO register(AlgorithmRegisterRequest request) {
        if (!StringUtils.hasText(request.getAlgorithmCode())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "algorithmCode 不能为空");
        }
        AnaAlgorithmRegistry existing = algorithmRegistryMapper.selectOne(
                new LambdaQueryWrapper<AnaAlgorithmRegistry>()
                        .eq(AnaAlgorithmRegistry::getAlgorithmCode, request.getAlgorithmCode())
                        .last("LIMIT 1"));
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "算法编码已存在");
        }
        AnaAlgorithmRegistry entity = new AnaAlgorithmRegistry();
        entity.setId(IdGenerator.nextId());
        entity.setAlgorithmCode(request.getAlgorithmCode());
        entity.setAlgorithmName(request.getAlgorithmName());
        entity.setVersion(request.getVersion());
        entity.setPackageRef(request.getPackageRef());
        entity.setStatus("ACTIVE");
        algorithmRegistryMapper.insert(entity);
        return toVO(entity);
    }

    private AlgorithmVO toVO(AnaAlgorithmRegistry entity) {
        AlgorithmVO vo = new AlgorithmVO();
        vo.setId(entity.getId());
        vo.setAlgorithmCode(entity.getAlgorithmCode());
        vo.setAlgorithmName(entity.getAlgorithmName());
        vo.setVersion(entity.getVersion());
        vo.setPackageRef(entity.getPackageRef());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
