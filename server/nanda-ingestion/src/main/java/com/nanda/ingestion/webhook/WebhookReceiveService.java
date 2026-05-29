package com.nanda.ingestion.webhook;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.JsonUtils;
import com.nanda.ingestion.adapter.StagingRecordDTO;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.WebhookReceiveResultVO;
import com.nanda.ingestion.domain.entity.StgBatch;
import com.nanda.ingestion.domain.entity.StgWebhookSubscription;
import com.nanda.ingestion.staging.StagingBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebhookReceiveService {

    private final WebhookSubscriptionService subscriptionService;
    private final StagingBatchService stagingBatchService;

    @Transactional
    public WebhookReceiveResultVO receive(Long subscriptionId, String body, String webhookSecret, String signature) {
        StgWebhookSubscription subscription = subscriptionService.requireActiveSubscription(subscriptionId);
        verifyAuth(subscription, body, webhookSecret, signature);

        List<StagingRecordDTO> records = parsePayload(body);
        StgBatch batch = stagingBatchService.createBatch(subscription.getId(), null, subscription.getOrgId(), records);

        WebhookReceiveResultVO vo = new WebhookReceiveResultVO();
        vo.setBatchId(batch.getId());
        vo.setRecordCount(batch.getRecordCount());
        vo.setStatus(batch.getStatus());
        return vo;
    }

    private void verifyAuth(StgWebhookSubscription subscription, String body, String webhookSecret, String signature) {
        if (!StringUtils.hasText(webhookSecret)) {
            throw new BusinessException(ErrorCode.INGESTION_WEBHOOK_AUTH_FAILED, "缺少 X-Webhook-Secret");
        }
        if (!WebhookSecretHasher.matches(webhookSecret, subscription.getSecretHash())) {
            throw new BusinessException(ErrorCode.INGESTION_WEBHOOK_AUTH_FAILED, "Webhook 密钥无效");
        }
        if (StringUtils.hasText(signature)) {
            String expected = "sha256=" + hmacSha256(body, webhookSecret);
            if (!expected.equalsIgnoreCase(signature.trim())) {
                throw new BusinessException(ErrorCode.INGESTION_WEBHOOK_AUTH_FAILED, "Webhook 签名无效");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<StagingRecordDTO> parsePayload(String body) {
        List<StagingRecordDTO> records = new ArrayList<StagingRecordDTO>();
        if (!StringUtils.hasText(body)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "请求体不能为空");
        }
        Object parsed = JsonUtils.fromJson(body, Object.class);
        if (parsed instanceof List) {
            List<Object> items = (List<Object>) parsed;
            int index = 0;
            for (Object item : items) {
                index++;
                records.add(toRecord(item, "WEBHOOK#" + index));
            }
        } else if (parsed instanceof Map) {
            records.add(toRecord(parsed, "WEBHOOK#1"));
        } else {
            StagingRecordDTO dto = new StagingRecordDTO();
            dto.setDomain("OTHER");
            dto.setSourceRef("WEBHOOK#1");
            dto.setRawPayload(body);
            dto.setParseStatus("OK");
            records.add(dto);
        }
        if (records.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "Webhook 载荷无有效记录");
        }
        return records;
    }

    private StagingRecordDTO toRecord(Object item, String sourceRef) {
        StagingRecordDTO dto = new StagingRecordDTO();
        dto.setDomain("OTHER");
        dto.setSourceRef(sourceRef);
        dto.setRawPayload(JsonUtils.toJson(item));
        dto.setParseStatus("OK");
        if (item instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) item;
            Object domain = map.get("domain");
            if (domain != null) {
                dto.setDomain(String.valueOf(domain));
            }
            Object ref = map.get("sourceRef");
            if (ref != null) {
                dto.setSourceRef(String.valueOf(ref));
            }
        }
        return dto;
    }

    private String hmacSha256(String body, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "签名计算失败");
        }
    }
}
