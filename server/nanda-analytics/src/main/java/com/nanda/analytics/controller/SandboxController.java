package com.nanda.analytics.controller;

import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.AlgorithmRegisterRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.AlgorithmVO;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.DatasetMountRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.DatasetVO;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.NotebookSaveRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.NotebookVO;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.SandboxJobSubmitRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.SandboxJobVO;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.SandboxSessionVO;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.ScriptTemplateCreateRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.ScriptTemplateUpdateRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.ScriptTemplateVO;
import com.nanda.analytics.sandbox.AlgorithmRegistryService;
import com.nanda.analytics.sandbox.DatasetMountService;
import com.nanda.analytics.sandbox.SandboxService;
import com.nanda.analytics.sandbox.ScriptTemplateService;
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

@Api(tags = "分析-沙箱")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/sandbox")
@RequiredArgsConstructor
public class SandboxController {

    private final SandboxService sandboxService;
    private final DatasetMountService datasetMountService;
    private final AlgorithmRegistryService algorithmRegistryService;
    private final ScriptTemplateService scriptTemplateService;

    @ApiOperation("创建/恢复沙箱会话")
    @PostMapping("/sessions")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<SandboxSessionVO> createSession() {
        return Result.ok(sandboxService.createSession());
    }

    @ApiOperation("沙箱会话状态")
    @GetMapping("/sessions/{sessionId}")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<SandboxSessionVO> getSession(@PathVariable Long sessionId) {
        return Result.ok(sandboxService.getSession(sessionId));
    }

    @ApiOperation("获取 Notebook")
    @GetMapping("/notebooks/{notebookId}")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<NotebookVO> getNotebook(
            @PathVariable String notebookId,
            @org.springframework.web.bind.annotation.RequestParam Long sessionId) {
        return Result.ok(sandboxService.getNotebook(sessionId, notebookId));
    }

    @ApiOperation("保存 Notebook")
    @PutMapping("/notebooks/{notebookId}")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<NotebookVO> saveNotebook(
            @PathVariable String notebookId,
            @org.springframework.web.bind.annotation.RequestParam Long sessionId,
            @RequestBody NotebookSaveRequest request) {
        return Result.ok(sandboxService.saveNotebook(sessionId, notebookId, request));
    }

    @ApiOperation("挂载脱敏数据集")
    @PostMapping("/datasets/mount")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<DatasetVO> mountDataset(@RequestBody DatasetMountRequest request) {
        return Result.ok(datasetMountService.mount(request));
    }

    @ApiOperation("提交沙箱分析作业")
    @PostMapping("/jobs")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<SandboxJobVO> submitJob(@RequestBody SandboxJobSubmitRequest request) {
        return Result.ok(sandboxService.submitJob(request));
    }

    @ApiOperation("沙箱作业状态")
    @GetMapping("/jobs/{jobId}")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<SandboxJobVO> getJob(@PathVariable Long jobId) {
        return Result.ok(sandboxService.getJob(jobId));
    }

    @ApiOperation("沙箱作业结果")
    @GetMapping("/jobs/{jobId}/result")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<SandboxJobVO> getJobResult(@PathVariable Long jobId) {
        return Result.ok(sandboxService.getJob(jobId));
    }

    @ApiOperation("取消沙箱作业")
    @PostMapping("/jobs/{jobId}/cancel")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<SandboxJobVO> cancelJob(@PathVariable Long jobId) {
        return Result.ok(sandboxService.cancelJob(jobId));
    }

    @ApiOperation("算法包列表")
    @GetMapping("/algorithms")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<List<AlgorithmVO>> listAlgorithms() {
        return Result.ok(algorithmRegistryService.list());
    }

    @ApiOperation("注册算法包")
    @PostMapping("/algorithms")
    @RequiresPermission("analytics:sandbox:manage")
    public Result<AlgorithmVO> registerAlgorithm(@RequestBody AlgorithmRegisterRequest request) {
        return Result.ok(algorithmRegistryService.register(request));
    }

    @ApiOperation("脚本模板列表")
    @GetMapping("/scripts")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<List<ScriptTemplateVO>> listScripts() {
        return Result.ok(scriptTemplateService.list());
    }

    @ApiOperation("获取脚本模板")
    @GetMapping("/scripts/{name}")
    @RequiresPermission("analytics:sandbox:execute")
    public Result<ScriptTemplateVO> getScript(@PathVariable String name) {
        return Result.ok(scriptTemplateService.getByCode(name));
    }

    @ApiOperation("创建脚本模板")
    @PostMapping("/scripts")
    @RequiresPermission("analytics:sandbox:manage")
    public Result<ScriptTemplateVO> createScript(@RequestBody ScriptTemplateCreateRequest request) {
        return Result.ok(scriptTemplateService.create(request));
    }

    @ApiOperation("更新脚本模板")
    @PutMapping("/scripts/{name}")
    @RequiresPermission("analytics:sandbox:manage")
    public Result<ScriptTemplateVO> updateScript(
            @PathVariable String name,
            @RequestBody ScriptTemplateUpdateRequest request) {
        return Result.ok(scriptTemplateService.update(name, request));
    }
}
