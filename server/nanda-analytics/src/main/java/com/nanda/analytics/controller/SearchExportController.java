package com.nanda.analytics.controller;

import com.nanda.analytics.domain.dto.AnalyticsDtos.CountNodeRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.CountNodeVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportApproveRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportCreateRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportDownloadVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportRejectRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportTaskVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ImportToProjectRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ImportToProjectResultVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchExecuteRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchResultVO;
import com.nanda.analytics.export.ExportApprovalService;
import com.nanda.analytics.search.SearchService;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "分析-检索")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/search")
@RequiredArgsConstructor
class SearchController {

    private final SearchService searchService;

    @ApiOperation("执行检索")
    @PostMapping("/query")
    @RequiresPermission("analytics:search:execute")
    public Result<SearchResultVO> query(@RequestBody SearchExecuteRequest request) {
        return Result.ok(searchService.execute(request));
    }

    @ApiOperation("条件节点患者数统计")
    @PostMapping("/count-nodes")
    @RequiresPermission("analytics:search:execute")
    public Result<List<CountNodeVO>> countNodes(@RequestBody CountNodeRequest request) {
        return Result.ok(searchService.countNodes(request.getQueryJson()));
    }

    @ApiOperation("智能联想")
    @GetMapping("/suggest")
    @RequiresPermission("analytics:search:execute")
    public Result<List<String>> suggest(@RequestParam String prefix) {
        return Result.ok(searchService.suggest(prefix));
    }
}

@Api(tags = "分析-导出")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/exports")
@RequiredArgsConstructor
class ExportController {

    private final ExportApprovalService exportApprovalService;

    @ApiOperation("导出任务列表")
    @GetMapping
    @RequiresPermission("analytics:export:create")
    public Result<PageResult<ExportTaskVO>> list(PageQuery query) {
        return Result.ok(exportApprovalService.list(query));
    }

    @ApiOperation("创建导出任务")
    @PostMapping
    @RequiresPermission("analytics:export:create")
    public Result<ExportTaskVO> create(@RequestBody ExportCreateRequest request) {
        return Result.ok(exportApprovalService.create(request));
    }

    @ApiOperation("导出任务详情")
    @GetMapping("/{id}")
    @RequiresPermission("analytics:export:create")
    public Result<ExportTaskVO> get(@PathVariable Long id) {
        return Result.ok(exportApprovalService.get(id));
    }

    @ApiOperation("提交审核")
    @PostMapping("/{id}/submit")
    @RequiresPermission("analytics:export:create")
    public Result<ExportTaskVO> submit(@PathVariable Long id) {
        return Result.ok(exportApprovalService.submit(id));
    }

    @ApiOperation("审核通过")
    @PostMapping("/{id}/approve")
    @RequiresPermission("analytics:export:approve")
    public Result<ExportTaskVO> approve(@PathVariable Long id, @RequestBody(required = false) ExportApproveRequest request) {
        return Result.ok(exportApprovalService.approve(id, request != null ? request : new ExportApproveRequest()));
    }

    @ApiOperation("审核驳回")
    @PostMapping("/{id}/reject")
    @RequiresPermission("analytics:export:approve")
    public Result<ExportTaskVO> reject(@PathVariable Long id, @RequestBody ExportRejectRequest request) {
        return Result.ok(exportApprovalService.reject(id, request));
    }

    @ApiOperation("下载导出文件")
    @GetMapping("/{id}/download")
    @RequiresPermission("analytics:export:create")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        ExportDownloadVO file = exportApprovalService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getContent());
    }

    @ApiOperation("导入科研项目")
    @PostMapping("/import-to-project")
    @RequiresPermission("analytics:export:create")
    public Result<ImportToProjectResultVO> importToProject(@RequestBody ImportToProjectRequest request) {
        return Result.ok(exportApprovalService.importToProject(request));
    }
}
