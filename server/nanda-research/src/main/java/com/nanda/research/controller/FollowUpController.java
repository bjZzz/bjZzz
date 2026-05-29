package com.nanda.research.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.research.domain.dto.ResearchDtos.FollowUpPlanCreateRequest;
import com.nanda.research.domain.dto.ResearchDtos.FollowUpPlanVO;
import com.nanda.research.domain.dto.ResearchDtos.FollowUpTaskVO;
import com.nanda.research.followup.FollowUpTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Api(tags = "科研-随访")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpTaskService followUpTaskService;

    @ApiOperation("随访计划列表")
    @GetMapping("/plans")
    @RequiresPermission("research:followup:manage")
    public Result<List<FollowUpPlanVO>> listPlans(@RequestParam(required = false) Long projectId) {
        return Result.ok(followUpTaskService.listPlans(projectId));
    }

    @ApiOperation("创建随访计划")
    @PostMapping("/plans")
    @RequiresPermission("research:followup:manage")
    public Result<FollowUpPlanVO> createPlan(@RequestBody FollowUpPlanCreateRequest request) {
        return Result.ok(followUpTaskService.createPlan(request));
    }

    @ApiOperation("随访任务列表")
    @GetMapping("/tasks")
    @RequiresPermission("research:followup:manage")
    public Result<PageResult<FollowUpTaskVO>> listTasks(PageQuery query,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore,
                                                        @RequestParam(required = false) Long projectId) {
        return Result.ok(followUpTaskService.listTasks(query, status, dueBefore, projectId));
    }

    @ApiOperation("发起随访")
    @PostMapping("/tasks/{taskId}/start")
    @RequiresPermission("research:followup:manage")
    public Result<FollowUpTaskVO> start(@PathVariable Long taskId) {
        return Result.ok(followUpTaskService.startTask(taskId));
    }

    @ApiOperation("完成随访")
    @PutMapping("/tasks/{taskId}/complete")
    @RequiresPermission("research:followup:manage")
    public Result<FollowUpTaskVO> complete(@PathVariable Long taskId, @RequestBody(required = false) Map<String, String> body) {
        String channel = body != null ? body.get("channel") : null;
        return Result.ok(followUpTaskService.completeTask(taskId, channel));
    }
}
