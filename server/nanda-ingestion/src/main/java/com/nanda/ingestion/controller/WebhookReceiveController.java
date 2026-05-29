package com.nanda.ingestion.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.WebhookReceiveResultVO;
import com.nanda.ingestion.webhook.WebhookReceiveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "数据采集-Webhook接收")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/ingestion/webhook")
@RequiredArgsConstructor
public class WebhookReceiveController {

    private final WebhookReceiveService webhookReceiveService;

    @ApiOperation("Webhook 准实时推送接收")
    @PostMapping("/{subscriptionId}/receive")
    public Result<WebhookReceiveResultVO> receive(
            @PathVariable Long subscriptionId,
            @RequestBody String body,
            @RequestHeader(value = "X-Webhook-Secret", required = false) String webhookSecret,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature) {
        return Result.ok(webhookReceiveService.receive(subscriptionId, body, webhookSecret, signature));
    }
}
