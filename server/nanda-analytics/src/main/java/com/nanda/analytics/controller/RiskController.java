package com.nanda.analytics.controller;

import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.RiskAssessRequest;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.RiskResultVO;
import com.nanda.analytics.risk.RiskService;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "分析-风险模型")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/risk-models")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @ApiOperation("支持的风险模型列表")
    @GetMapping
    @RequiresPermission("analytics:risk:execute")
    public Result<List<String>> models() {
        return Result.ok(riskService.supportedModels());
    }

    @ApiOperation("执行风险模型评估")
    @PostMapping("/{modelCode}")
    @RequiresPermission("analytics:risk:execute")
    public Result<RiskResultVO> assess(@PathVariable String modelCode, @RequestBody RiskAssessRequest request) {
        return Result.ok(riskService.assess(modelCode, request));
    }

    @ApiOperation("患者风险评估历史")
    @GetMapping("/assessments")
    @RequiresPermission("analytics:risk:execute")
    public Result<List<RiskResultVO>> history(@RequestParam Long empiId) {
        return Result.ok(riskService.historyByEmpi(empiId));
    }
}
