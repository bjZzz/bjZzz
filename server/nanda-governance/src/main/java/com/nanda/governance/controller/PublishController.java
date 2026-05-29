package com.nanda.governance.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.governance.domain.dto.PublishRuleCreateRequest;
import com.nanda.governance.domain.dto.PublishRuleVO;
import com.nanda.governance.publish.PublishRuleService;
import com.nanda.governance.publish.PublishingPipeline;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "治理-入库发布")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/governance/publish")
@RequiredArgsConstructor
public class PublishController {

    private final PublishRuleService publishRuleService;
    private final PublishingPipeline publishingPipeline;

    @ApiOperation("入库规则列表")
    @GetMapping("/rules")
    @RequiresPermission("governance:publish:execute")
    public Result<List<PublishRuleVO>> listRules() {
        return Result.ok(publishRuleService.list());
    }

    @ApiOperation("创建入库规则")
    @PostMapping("/rules")
    @RequiresPermission("governance:publish:execute")
    public Result<PublishRuleVO> createRule(@Valid @RequestBody PublishRuleCreateRequest request) {
        return Result.ok(publishRuleService.create(request));
    }

    @ApiOperation("手动触发批次发布")
    @PostMapping("/tasks/{batchId}/execute")
    @RequiresPermission("governance:publish:execute")
    public Result<Void> execute(@PathVariable Long batchId) {
        publishingPipeline.processBatch(batchId);
        return Result.ok(null);
    }
}
