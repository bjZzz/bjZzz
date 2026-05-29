package com.nanda.asset.controller;

import com.nanda.asset.comorbidity.ComorbidityService;
import com.nanda.asset.domain.dto.AssetDtos.ComorbidityPatientDetailVO;
import com.nanda.asset.domain.dto.AssetDtos.ComorbidityRuleCreateRequest;
import com.nanda.asset.domain.dto.AssetDtos.ComorbidityRuleVO;
import com.nanda.asset.domain.dto.AssetDtos.ComorbidityViewVO;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
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

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "资产-共病库")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/comorbidity")
@RequiredArgsConstructor
public class ComorbidityController {

    private final ComorbidityService comorbidityService;

    @ApiOperation("共病规则列表")
    @GetMapping("/rules")
    @RequiresPermission("asset:comorbidity:read")
    public Result<List<ComorbidityRuleVO>> listRules() {
        return Result.ok(comorbidityService.listRules());
    }

    @ApiOperation("创建共病规则")
    @PostMapping("/rules")
    @RequiresPermission("asset:comorbidity:write")
    public Result<ComorbidityRuleVO> createRule(@Valid @RequestBody ComorbidityRuleCreateRequest request) {
        return Result.ok(comorbidityService.createRule(request));
    }

    @ApiOperation("刷新共病视图")
    @PostMapping("/rules/{ruleId}/refresh")
    @RequiresPermission("asset:comorbidity:write")
    public Result<Map<String, Object>> refresh(@PathVariable Long ruleId) {
        int refreshed = comorbidityService.refreshRule(ruleId);
        return Result.ok(java.util.Collections.singletonMap("refreshedEmpiCount", refreshed));
    }

    @ApiOperation("共病视图列表")
    @GetMapping("/views")
    @RequiresPermission("asset:comorbidity:read")
    public Result<PageResult<ComorbidityViewVO>> listViews(PageQuery query,
                                                            @RequestParam(required = false) Long ruleId) {
        return Result.ok(comorbidityService.listViews(query, ruleId));
    }

    @ApiOperation("跨病种患者详情")
    @GetMapping("/views/{viewId}/patients/{empiId}")
    @RequiresPermission("asset:comorbidity:read")
    public Result<ComorbidityPatientDetailVO> patientDetail(@PathVariable Long viewId,
                                                               @PathVariable Long empiId) {
        return Result.ok(comorbidityService.getPatientDetail(viewId, empiId));
    }
}
