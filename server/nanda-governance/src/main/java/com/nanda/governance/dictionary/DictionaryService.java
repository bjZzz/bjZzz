package com.nanda.governance.dictionary;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.event.DictionaryChangedEvent;
import com.nanda.common.util.IdGenerator;
import com.nanda.governance.domain.dto.DictDiagnosisCreateRequest;
import com.nanda.governance.domain.dto.DictDiagnosisVO;
import com.nanda.governance.domain.entity.GovDictDiagnosis;
import com.nanda.governance.mapper.GovDictDiagnosisMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final GovDictDiagnosisMapper govDictDiagnosisMapper;
    private final ApplicationEventPublisher eventPublisher;

    public List<DictDiagnosisVO> listDiagnosis() {
        Long orgId = AuthContextHolder.get().getOrgId();
        List<GovDictDiagnosis> list = govDictDiagnosisMapper.selectList(new LambdaQueryWrapper<GovDictDiagnosis>()
                .eq(GovDictDiagnosis::getDeleted, 0)
                .and(w -> w.eq(GovDictDiagnosis::getOrgId, orgId).or().isNull(GovDictDiagnosis::getOrgId)));
        List<DictDiagnosisVO> result = new ArrayList<DictDiagnosisVO>();
        for (GovDictDiagnosis item : list) {
            result.add(toVO(item));
        }
        return result;
    }

    public DictDiagnosisVO createDiagnosis(DictDiagnosisCreateRequest request) {
        Long orgId = AuthContextHolder.get().getOrgId();
        long exists = govDictDiagnosisMapper.selectCount(new LambdaQueryWrapper<GovDictDiagnosis>()
                .eq(GovDictDiagnosis::getCode, request.getCode())
                .eq(GovDictDiagnosis::getOrgId, orgId)
                .eq(GovDictDiagnosis::getDeleted, 0));
        if (exists > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "诊断编码已存在");
        }
        GovDictDiagnosis item = new GovDictDiagnosis();
        item.setId(IdGenerator.nextId());
        item.setCode(request.getCode());
        item.setNameZh(request.getNameZh());
        item.setNameEn(request.getNameEn());
        item.setOrgId(orgId);
        item.setDeleted(0);
        govDictDiagnosisMapper.insert(item);
        eventPublisher.publishEvent(new DictionaryChangedEvent(this, "DIAGNOSIS",
                Collections.singletonList(request.getCode()), orgId));
        return toVO(item);
    }

    private DictDiagnosisVO toVO(GovDictDiagnosis item) {
        DictDiagnosisVO vo = new DictDiagnosisVO();
        vo.setId(item.getId());
        vo.setCode(item.getCode());
        vo.setNameZh(item.getNameZh());
        vo.setNameEn(item.getNameEn());
        return vo;
    }
}
