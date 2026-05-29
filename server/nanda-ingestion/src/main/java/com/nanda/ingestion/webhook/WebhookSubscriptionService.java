package com.nanda.ingestion.webhook;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.util.IdGenerator;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.WebhookSubscriptionCreateRequest;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.WebhookSubscriptionVO;
import com.nanda.ingestion.domain.entity.StgWebhookSubscription;
import com.nanda.ingestion.mapper.StgWebhookSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebhookSubscriptionService {

    private final StgWebhookSubscriptionMapper subscriptionMapper;

    public List<WebhookSubscriptionVO> list() {
        Long orgId = requireOrgId();
        List<StgWebhookSubscription> rows = subscriptionMapper.selectList(
                new LambdaQueryWrapper<StgWebhookSubscription>()
                        .eq(StgWebhookSubscription::getOrgId, orgId)
                        .orderByDesc(StgWebhookSubscription::getCreatedAt));
        List<WebhookSubscriptionVO> result = new ArrayList<WebhookSubscriptionVO>();
        for (StgWebhookSubscription row : rows) {
            result.add(toVO(row, null));
        }
        return result;
    }

    public WebhookSubscriptionVO getById(Long id) {
        return toVO(requireSubscription(id), null);
    }

    @Transactional
    public WebhookSubscriptionVO create(WebhookSubscriptionCreateRequest request) {
        Long orgId = requireOrgId();
        String secret = WebhookSecretHasher.generateSecret();
        StgWebhookSubscription entity = new StgWebhookSubscription();
        entity.setId(IdGenerator.nextId());
        entity.setEndpointUrl(request.getEndpointUrl());
        entity.setSecretHash(WebhookSecretHasher.hash(secret));
        entity.setOrgId(orgId);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        subscriptionMapper.insert(entity);
        return toVO(entity, secret);
    }

    @Transactional
    public void disable(Long id) {
        StgWebhookSubscription entity = requireSubscription(id);
        entity.setStatus("DISABLED");
        subscriptionMapper.updateById(entity);
    }

    public StgWebhookSubscription requireActiveSubscription(Long id) {
        StgWebhookSubscription entity = subscriptionMapper.selectById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Webhook 订阅不存在");
        }
        if (!"ACTIVE".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE, "Webhook 订阅已停用");
        }
        return entity;
    }

    private StgWebhookSubscription requireSubscription(Long id) {
        StgWebhookSubscription entity = subscriptionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Webhook 订阅不存在");
        }
        Long orgId = requireOrgId();
        if (!entity.getOrgId().equals(orgId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该订阅");
        }
        return entity;
    }

    private WebhookSubscriptionVO toVO(StgWebhookSubscription entity, String plainSecret) {
        WebhookSubscriptionVO vo = new WebhookSubscriptionVO();
        vo.setId(entity.getId());
        vo.setEndpointUrl(entity.getEndpointUrl());
        vo.setReceiveUrl(CommonConstants.API_PREFIX + "/ingestion/webhook/" + entity.getId() + "/receive");
        vo.setSecret(plainSecret);
        vo.setOrgId(entity.getOrgId());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private Long requireOrgId() {
        if (AuthContextHolder.get() == null || AuthContextHolder.get().getOrgId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少机构上下文");
        }
        return AuthContextHolder.get().getOrgId();
    }
}
