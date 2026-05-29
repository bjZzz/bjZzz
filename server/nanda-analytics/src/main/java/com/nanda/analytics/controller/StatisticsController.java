package com.nanda.analytics.controller;

import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.StatRequest;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.StatResultVO;
import com.nanda.analytics.statistics.StatService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "分析-统计")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/analytics/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatService statService;

    @ApiOperation("统计方法列表")
    @GetMapping("/methods")
    @RequiresPermission("analytics:stat:execute")
    public Result<Map<String, Object>> methods() {
        return Result.ok(statService.listMethods());
    }

    @ApiOperation("执行统计方法")
    @PostMapping("/{method}")
    @RequiresPermission("analytics:stat:execute")
    public Result<StatResultVO> execute(@PathVariable String method, @RequestBody StatRequest request) {
        return Result.ok(statService.execute(method, request));
    }
}
