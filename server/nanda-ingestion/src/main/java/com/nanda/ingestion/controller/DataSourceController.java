package com.nanda.ingestion.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.ingestion.datasource.DataSourceService;
import com.nanda.ingestion.domain.dto.ConnectionTestVO;
import com.nanda.ingestion.domain.dto.DataSourceCreateRequest;
import com.nanda.ingestion.domain.dto.DataSourceVO;
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

@Api(tags = "数据采集-数据源")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/ingestion/datasources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @ApiOperation("数据源列表")
    @GetMapping
    @RequiresPermission("ingestion:datasource:read")
    public Result<PageResult<DataSourceVO>> list(PageQuery query) {
        return Result.ok(dataSourceService.list(query));
    }

    @ApiOperation("数据源详情")
    @GetMapping("/{id}")
    @RequiresPermission("ingestion:datasource:read")
    public Result<DataSourceVO> get(@PathVariable Long id) {
        return Result.ok(dataSourceService.getById(id));
    }

    @ApiOperation("创建数据源")
    @PostMapping
    @RequiresPermission("ingestion:datasource:write")
    public Result<DataSourceVO> create(@Valid @RequestBody DataSourceCreateRequest request) {
        return Result.ok(dataSourceService.create(request));
    }

    @ApiOperation("测试连接")
    @PostMapping("/{id}/test-connection")
    @RequiresPermission("ingestion:datasource:write")
    public Result<ConnectionTestVO> testConnection(@PathVariable Long id) {
        return Result.ok(dataSourceService.testConnection(id));
    }
}
