package com.nanda.analytics.controller;

import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.ReportCreateRequest;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.ReportDownloadVO;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.ReportVO;
import com.nanda.analytics.report.ReportService;
import com.nanda.common.core.constant.CommonConstants;
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
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "分析-评估报告")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @ApiOperation("生成风险评估 PDF")
    @PostMapping("/risk-assessment")
    @RequiresPermission("analytics:report:manage")
    public Result<ReportVO> generate(@RequestBody ReportCreateRequest request) {
        return Result.ok(reportService.generateRiskAssessment(request));
    }

    @ApiOperation("报告详情")
    @GetMapping("/{id}")
    @RequiresPermission("analytics:report:manage")
    public Result<ReportVO> get(@PathVariable Long id) {
        return Result.ok(reportService.get(id));
    }

    @ApiOperation("下载报告 PDF")
    @GetMapping("/{id}/download")
    @RequiresPermission("analytics:report:manage")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        ReportDownloadVO file = reportService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getContent());
    }
}
