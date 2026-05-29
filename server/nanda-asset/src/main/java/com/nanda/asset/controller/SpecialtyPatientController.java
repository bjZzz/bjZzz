package com.nanda.asset.controller;

import com.nanda.asset.domain.dto.AssetDtos.Patient360VO;
import com.nanda.asset.domain.dto.AssetDtos.SpecialtyOverviewVO;
import com.nanda.asset.domain.dto.AssetDtos.SpecialtyPatientVO;
import com.nanda.asset.domain.dto.AssetDtos.TimelineEventVO;
import com.nanda.asset.domain.enums.SpecialtyType;
import com.nanda.asset.specialty.Patient360Service;
import com.nanda.asset.specialty.SpecialtyPatientService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "资产-专病库")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/specialty")
@RequiredArgsConstructor
public class SpecialtyPatientController {

    private final SpecialtyPatientService specialtyPatientService;
    private final Patient360Service patient360Service;

    @ApiOperation("专病库概览")
    @GetMapping("/{type}/overview")
    @RequiresPermission("asset:specialty:read")
    public Result<SpecialtyOverviewVO> overview(@PathVariable String type) {
        return Result.ok(specialtyPatientService.overview(SpecialtyType.fromPath(type)));
    }

    @ApiOperation("专病患者列表")
    @GetMapping("/{type}/patients")
    @RequiresPermission("asset:specialty:read")
    public Result<PageResult<SpecialtyPatientVO>> listPatients(@PathVariable String type, PageQuery query) {
        return Result.ok(specialtyPatientService.listPatients(SpecialtyType.fromPath(type), query));
    }

    @ApiOperation("专病患者详情")
    @GetMapping("/{type}/patients/{recordId}")
    @RequiresPermission("asset:specialty:read")
    public Result<SpecialtyPatientVO> getPatient(@PathVariable String type, @PathVariable Long recordId) {
        return Result.ok(specialtyPatientService.getPatient(SpecialtyType.fromPath(type), recordId));
    }

    @ApiOperation("患者360视图")
    @GetMapping("/{type}/patients/{recordId}/360")
    @RequiresPermission("asset:specialty:read")
    public Result<Patient360VO> patient360(@PathVariable String type, @PathVariable Long recordId) {
        return Result.ok(patient360Service.build360(SpecialtyType.fromPath(type), recordId));
    }

    @ApiOperation("诊疗时间轴")
    @GetMapping("/{type}/patients/{recordId}/timeline")
    @RequiresPermission("asset:specialty:read")
    public Result<List<TimelineEventVO>> timeline(@PathVariable String type, @PathVariable Long recordId) {
        return Result.ok(patient360Service.getTimeline(SpecialtyType.fromPath(type), recordId));
    }
}
