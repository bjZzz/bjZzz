package com.nanda.ingestion.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.WebhookSubscriptionCreateRequest;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.WebhookSubscriptionVO;
import com.nanda.ingestion.webhook.WebhookSubscriptionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "数据采集-Webhook订阅")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/ingestion/webhook/subscriptions")
@RequiredArgsConstructor
public class WebhookSubscriptionController {

    private final WebhookSubscriptionService webhookSubscriptionService;

    @ApiOperation("Webhook 订阅列表")
    @GetMapping
    @RequiresPermission("ingestion:webhook:manage")
    public Result<List<WebhookSubscriptionVO>> list() {
        return Result.ok(webhookSubscriptionService.list());
    }

    @ApiOperation("Webhook 订阅详情")
    @GetMapping("/{id}")
    @RequiresPermission("ingestion:webhook:manage")
    public Result<WebhookSubscriptionVO> get(@PathVariable Long id) {
        return Result.ok(webhookSubscriptionService.getById(id));
    }

    @ApiOperation("创建 Webhook 订阅")
    @PostMapping
    @RequiresPermission("ingestion:webhook:manage")
    public Result<WebhookSubscriptionVO> create(@Valid @RequestBody WebhookSubscriptionCreateRequest request) {
        return Result.ok(webhookSubscriptionService.create(request));
    }

    @ApiOperation("停用 Webhook 订阅")
    @PutMapping("/{id}/disable")
    @RequiresPermission("ingestion:webhook:manage")
    public Result<Void> disable(@PathVariable Long id) {
        webhookSubscriptionService.disable(id);
        return Result.ok();
    }
}
