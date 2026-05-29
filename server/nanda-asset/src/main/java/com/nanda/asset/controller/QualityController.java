package com.nanda.asset.controller;

import com.nanda.asset.domain.dto.AssetDtos.QcDashboardVO;
import com.nanda.asset.domain.dto.AssetDtos.QcReviewRequest;
import com.nanda.asset.domain.dto.AssetDtos.QcReviewTaskVO;
import com.nanda.asset.domain.dto.AssetDtos.QcSampleBatchCreateRequest;
import com.nanda.asset.domain.dto.AssetDtos.QcSampleBatchVO;
import com.nanda.asset.quality.QcReviewService;
import com.nanda.asset.quality.QcSampleService;
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
import java.util.Map;

@Api(tags = "资产-质控")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/quality")
@RequiredArgsConstructor
public class QualityController {

    private final QcSampleService qcSampleService;
    private final QcReviewService qcReviewService;

    @ApiOperation("六维质控仪表盘")
    @GetMapping("/dashboard")
    @RequiresPermission("asset:qc:read")
    public Result<QcDashboardVO> dashboard() {
        return Result.ok(qcSampleService.dashboard());
    }

    @ApiOperation("创建抽样批次")
    @PostMapping("/sampling/batches")
    @RequiresPermission("asset:qc:write")
    public Result<QcSampleBatchVO> createSampleBatch(@Valid @RequestBody QcSampleBatchCreateRequest request) {
        return Result.ok(qcSampleService.createSampleBatch(request));
    }

    @ApiOperation("待复核任务列表")
    @GetMapping("/review-tasks")
    @RequiresPermission("asset:qc:read")
    public Result<PageResult<QcReviewTaskVO>> listReviewTasks(PageQuery query) {
        return Result.ok(qcSampleService.listReviewTasks(query));
    }

    @ApiOperation("在线审核")
    @PostMapping("/review-tasks/{taskId}/review")
    @RequiresPermission("asset:qc:write")
    public Result<QcReviewTaskVO> review(@PathVariable Long taskId,
                                          @Valid @RequestBody QcReviewRequest request) {
        return Result.ok(qcReviewService.review(taskId, request));
    }

    @ApiOperation("差异比对")
    @GetMapping("/compare")
    @RequiresPermission("asset:qc:read")
    public Result<Map<String, Object>> compare(@RequestParam Long patientId) {
        return Result.ok(qcReviewService.compare(patientId));
    }
}
