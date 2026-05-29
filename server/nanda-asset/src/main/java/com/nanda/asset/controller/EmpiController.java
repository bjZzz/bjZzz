package com.nanda.asset.controller;

import com.nanda.asset.domain.dto.AssetDtos.EmpiMatchCandidateVO;
import com.nanda.asset.domain.dto.AssetDtos.EmpiMatchRequest;
import com.nanda.asset.domain.dto.AssetDtos.EmpiMatchResultVO;
import com.nanda.asset.domain.dto.AssetDtos.EmpiMatchRuleVO;
import com.nanda.asset.domain.dto.AssetDtos.EmpiPatientVO;
import com.nanda.asset.domain.dto.AssetDtos.TimelineEventVO;
import com.nanda.asset.empi.EmpiMatchService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "资产-EMPI")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/empi")
@RequiredArgsConstructor
public class EmpiController {

    private final EmpiMatchService empiMatchService;

    @ApiOperation("EMPI患者详情")
    @GetMapping("/patients/{empiId}")
    @RequiresPermission("asset:empi:read")
    public Result<EmpiPatientVO> getPatient(@PathVariable Long empiId) {
        return Result.ok(empiMatchService.getPatient(empiId));
    }

    @ApiOperation("就诊时间轴")
    @GetMapping("/patients/{empiId}/timeline")
    @RequiresPermission("asset:empi:read")
    public Result<List<TimelineEventVO>> timeline(@PathVariable Long empiId) {
        return Result.ok(empiMatchService.getTimeline(empiId));
    }

    @ApiOperation("触发患者匹配")
    @PostMapping("/match")
    @RequiresPermission("asset:empi:match")
    public Result<EmpiMatchResultVO> match(@Valid @RequestBody EmpiMatchRequest request) {
        return Result.ok(empiMatchService.match(request));
    }

    @ApiOperation("待确认匹配候选")
    @GetMapping("/match-candidates")
    @RequiresPermission("asset:empi:read")
    public Result<PageResult<EmpiMatchCandidateVO>> listCandidates(PageQuery query) {
        return Result.ok(empiMatchService.listCandidates(query));
    }

    @ApiOperation("确认合并候选")
    @PostMapping("/match-candidates/{id}/confirm")
    @RequiresPermission("asset:empi:match")
    public Result<EmpiMatchResultVO> confirm(@PathVariable Long id) {
        return Result.ok(empiMatchService.confirmCandidate(id));
    }

    @ApiOperation("拒绝合并候选")
    @PostMapping("/match-candidates/{id}/reject")
    @RequiresPermission("asset:empi:match")
    public Result<EmpiMatchResultVO> reject(@PathVariable Long id) {
        return Result.ok(empiMatchService.rejectCandidate(id));
    }

    @ApiOperation("获取匹配规则")
    @GetMapping("/rules")
    @RequiresPermission("asset:empi:read")
    public Result<EmpiMatchRuleVO> getRule() {
        return Result.ok(empiMatchService.getMatchRule());
    }

    @ApiOperation("更新匹配权重")
    @PutMapping("/rules")
    @RequiresPermission("asset:empi:match")
    public Result<EmpiMatchRuleVO> updateRule(@RequestBody Map<String, Object> body) {
        String ruleConfigJson = body.get("ruleConfigJson") != null
                ? String.valueOf(body.get("ruleConfigJson"))
                : com.nanda.common.util.JsonUtils.toJson(body);
        return Result.ok(empiMatchService.updateMatchRule(ruleConfigJson));
    }
}
