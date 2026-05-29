package com.nanda.ingestion.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.ingestion.domain.dto.StagingBatchVO;
import com.nanda.ingestion.domain.dto.SyncJobCreateRequest;
import com.nanda.ingestion.domain.dto.SyncJobVO;
import com.nanda.ingestion.sync.SyncJobService;
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

@Api(tags = "数据采集-同步任务")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/ingestion/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {

    private final SyncJobService syncJobService;

    @ApiOperation("同步任务列表")
    @GetMapping
    @RequiresPermission("ingestion:datasource:read")
    public Result<List<SyncJobVO>> list() {
        return Result.ok(syncJobService.list());
    }

    @ApiOperation("创建同步任务")
    @PostMapping
    @RequiresPermission("ingestion:datasource:write")
    public Result<SyncJobVO> create(@Valid @RequestBody SyncJobCreateRequest request) {
        return Result.ok(syncJobService.create(request));
    }

    @ApiOperation("手动执行同步")
    @PostMapping("/{id}/start")
    @RequiresPermission("ingestion:sync:execute")
    public Result<StagingBatchVO> start(@PathVariable Long id) {
        return Result.ok(syncJobService.start(id));
    }
}
