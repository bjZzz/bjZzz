package com.nanda.research.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.research.cohort.CohortService;
import com.nanda.research.cohort.RandomizationService;
import com.nanda.research.domain.dto.ResearchDtos.CohortCreateRequest;
import com.nanda.research.domain.dto.ResearchDtos.CohortMemberEnrollRequest;
import com.nanda.research.domain.dto.ResearchDtos.CohortMemberVO;
import com.nanda.research.domain.dto.ResearchDtos.CohortScreenResultVO;
import com.nanda.research.domain.dto.ResearchDtos.CohortVO;
import com.nanda.research.domain.dto.ResearchDtos.ProjectCreateRequest;
import com.nanda.research.domain.dto.ResearchDtos.ProjectMemberAddRequest;
import com.nanda.research.domain.dto.ResearchDtos.ProjectMemberVO;
import com.nanda.research.domain.dto.ResearchDtos.ProjectProgressVO;
import com.nanda.research.domain.dto.ResearchDtos.ProjectTransitionRequest;
import com.nanda.research.domain.dto.ResearchDtos.ProjectVO;
import com.nanda.research.project.ProjectMemberService;
import com.nanda.research.project.ProjectProgressService;
import com.nanda.research.project.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(tags = "科研-项目")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final ProjectProgressService projectProgressService;

    @ApiOperation("项目列表")
    @GetMapping
    @RequiresPermission("research:project:read")
    public Result<PageResult<ProjectVO>> list(PageQuery query) {
        return Result.ok(projectService.list(query));
    }

    @ApiOperation("项目详情")
    @GetMapping("/{id}")
    @RequiresPermission("research:project:read")
    public Result<ProjectVO> get(@PathVariable Long id) {
        return Result.ok(projectService.get(id));
    }

    @ApiOperation("创建项目")
    @PostMapping
    @RequiresPermission("research:project:write")
    public Result<ProjectVO> create(@RequestBody ProjectCreateRequest request) {
        return Result.ok(projectService.create(request));
    }

    @ApiOperation("项目状态变更")
    @PutMapping("/{id}/status")
    @RequiresPermission("research:project:write")
    public Result<ProjectVO> transition(@PathVariable Long id, @RequestBody ProjectTransitionRequest request) {
        return Result.ok(projectService.transition(id, request));
    }

    @ApiOperation("项目进度")
    @GetMapping("/{id}/progress")
    @RequiresPermission("research:project:read")
    public Result<ProjectProgressVO> progress(@PathVariable Long id) {
        return Result.ok(projectProgressService.getProgress(id));
    }

    @ApiOperation("项目成员列表")
    @GetMapping("/{id}/members")
    @RequiresPermission("research:project:read")
    public Result<List<ProjectMemberVO>> listMembers(@PathVariable Long id) {
        return Result.ok(projectMemberService.listMembers(id));
    }

    @ApiOperation("添加项目成员")
    @PostMapping("/{id}/members")
    @RequiresPermission("research:project:write")
    public Result<ProjectMemberVO> addMember(@PathVariable Long id, @RequestBody ProjectMemberAddRequest request) {
        return Result.ok(projectMemberService.addMember(id, request));
    }

    @ApiOperation("移除项目成员")
    @DeleteMapping("/{id}/members/{memberId}")
    @RequiresPermission("research:project:write")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        projectMemberService.removeMember(id, memberId);
        return Result.ok(null);
    }
}

@Api(tags = "科研-队列")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/cohorts")
@RequiredArgsConstructor
class CohortController {

    private final CohortService cohortService;
    private final RandomizationService randomizationService;

    @ApiOperation("队列列表")
    @GetMapping
    @RequiresPermission("research:cohort:manage")
    public Result<PageResult<CohortVO>> list(PageQuery query, @RequestParam(required = false) Long projectId) {
        return Result.ok(cohortService.list(query, projectId));
    }

    @ApiOperation("队列详情")
    @GetMapping("/{id}")
    @RequiresPermission("research:cohort:manage")
    public Result<CohortVO> get(@PathVariable Long id) {
        return Result.ok(cohortService.get(id));
    }

    @ApiOperation("创建队列")
    @PostMapping
    @RequiresPermission("research:cohort:manage")
    public Result<CohortVO> create(@RequestBody CohortCreateRequest request) {
        return Result.ok(cohortService.create(request));
    }

    @ApiOperation("更新纳排规则")
    @PutMapping("/{id}/rules")
    @RequiresPermission("research:cohort:manage")
    public Result<CohortVO> updateRules(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.ok(cohortService.updateRules(id, body.get("ruleJson")));
    }

    @ApiOperation("自动筛选入组")
    @PostMapping("/{id}/screen")
    @RequiresPermission("research:cohort:manage")
    public Result<CohortScreenResultVO> screen(@PathVariable Long id) {
        return Result.ok(cohortService.screen(id));
    }

    @ApiOperation("队列成员列表")
    @GetMapping("/{id}/members")
    @RequiresPermission("research:cohort:manage")
    public Result<PageResult<CohortMemberVO>> listMembers(@PathVariable Long id, PageQuery query) {
        return Result.ok(cohortService.listMembers(id, query));
    }

    @ApiOperation("手动入组")
    @PostMapping("/{id}/members")
    @RequiresPermission("research:cohort:manage")
    public Result<CohortMemberVO> enroll(@PathVariable Long id, @RequestBody CohortMemberEnrollRequest request) {
        return Result.ok(cohortService.enrollMember(id, request));
    }

    @ApiOperation("移除成员")
    @DeleteMapping("/{id}/members/{memberId}")
    @RequiresPermission("research:cohort:manage")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        cohortService.removeMember(id, memberId);
        return Result.ok(null);
    }

    @ApiOperation("随机分组")
    @PostMapping("/{id}/randomize")
    @RequiresPermission("research:cohort:manage")
    public Result<RandomizationService.RandomizeResult> randomize(@PathVariable Long id,
                                                                  @RequestBody RandomizationService.RandomizeRequest request) {
        return Result.ok(randomizationService.randomize(id, request));
    }
}
