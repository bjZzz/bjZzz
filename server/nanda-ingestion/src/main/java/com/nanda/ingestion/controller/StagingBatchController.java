package com.nanda.ingestion.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.ingestion.domain.dto.StagingBatchVO;
import com.nanda.ingestion.staging.StagingBatchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "数据采集-Staging批次")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/ingestion/staging/batches")
@RequiredArgsConstructor
public class StagingBatchController {

    private final StagingBatchService stagingBatchService;

    @ApiOperation("批次列表")
    @GetMapping
    @RequiresPermission("ingestion:staging:read")
    public Result<PageResult<StagingBatchVO>> list(PageQuery query,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String dateFrom) {
        return Result.ok(stagingBatchService.list(query, status, dateFrom));
    }

    @ApiOperation("批次详情")
    @GetMapping("/{id}")
    @RequiresPermission("ingestion:staging:read")
    public Result<StagingBatchVO> get(@PathVariable Long id) {
        return Result.ok(stagingBatchService.getById(id));
    }

    @ApiOperation("重试批次")
    @PostMapping("/{id}/retry")
    @RequiresPermission("ingestion:sync:execute")
    public Result<StagingBatchVO> retry(@PathVariable Long id) {
        return Result.ok(stagingBatchService.retry(id));
    }
}
