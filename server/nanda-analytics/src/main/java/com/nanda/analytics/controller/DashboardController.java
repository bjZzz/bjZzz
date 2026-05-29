package com.nanda.analytics.controller;

import com.nanda.analytics.dashboard.DashboardService;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.DashboardCreateRequest;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.DashboardDataVO;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.DashboardVO;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
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

import java.util.List;

@Api(tags = "分析-仪表盘")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @ApiOperation("仪表盘列表")
    @GetMapping
    @RequiresPermission("analytics:dashboard:manage")
    public Result<List<DashboardVO>> list() {
        return Result.ok(dashboardService.list());
    }

    @ApiOperation("创建仪表盘")
    @PostMapping
    @RequiresPermission("analytics:dashboard:manage")
    public Result<DashboardVO> create(@RequestBody DashboardCreateRequest request) {
        return Result.ok(dashboardService.create(request));
    }

    @ApiOperation("更新仪表盘布局")
    @PutMapping("/{id}")
    @RequiresPermission("analytics:dashboard:manage")
    public Result<DashboardVO> update(@PathVariable Long id, @RequestBody DashboardCreateRequest request) {
        return Result.ok(dashboardService.updateLayout(id, request));
    }

    @ApiOperation("仪表盘数据聚合")
    @GetMapping("/data")
    @RequiresPermission("analytics:dashboard:manage")
    public Result<DashboardDataVO> data() {
        return Result.ok(dashboardService.data());
    }
}
