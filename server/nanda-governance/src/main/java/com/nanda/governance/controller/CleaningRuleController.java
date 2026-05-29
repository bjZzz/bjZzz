package com.nanda.governance.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.governance.cleaning.CleaningRuleService;
import com.nanda.governance.domain.dto.CleaningRuleCreateRequest;
import com.nanda.governance.domain.dto.CleaningRuleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "治理-清洗规则")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/governance/cleaning-rules")
@RequiredArgsConstructor
public class CleaningRuleController {

    private final CleaningRuleService cleaningRuleService;

    @ApiOperation("清洗规则列表")
    @GetMapping
    @RequiresPermission("governance:dict:read")
    public Result<List<CleaningRuleVO>> list() {
        return Result.ok(cleaningRuleService.list());
    }

    @ApiOperation("创建清洗规则")
    @PostMapping
    @RequiresPermission("governance:dict:write")
    public Result<CleaningRuleVO> create(@Valid @RequestBody CleaningRuleCreateRequest request) {
        return Result.ok(cleaningRuleService.create(request));
    }
}
