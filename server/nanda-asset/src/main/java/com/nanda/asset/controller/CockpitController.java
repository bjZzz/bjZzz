package com.nanda.asset.controller;

import com.nanda.asset.domain.dto.AssetDtos.CockpitSummaryVO;
import com.nanda.asset.specialty.SpecialtyOverviewService;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "资产-驾驶舱")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/specialty/cockpit")
@RequiredArgsConstructor
public class CockpitController {

    private final SpecialtyOverviewService specialtyOverviewService;

    @ApiOperation("驾驶舱简报")
    @GetMapping("/summary")
    @RequiresPermission("asset:specialty:read")
    public Result<CockpitSummaryVO> summary() {
        return Result.ok(specialtyOverviewService.cockpitSummary());
    }
}
