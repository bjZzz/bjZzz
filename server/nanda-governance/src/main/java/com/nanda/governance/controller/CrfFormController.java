package com.nanda.governance.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.governance.crf.CrfFormService;
import com.nanda.governance.domain.dto.CrfFormCreateRequest;
import com.nanda.governance.domain.dto.CrfFormVO;
import com.nanda.governance.domain.dto.CrfResponseSubmitRequest;
import com.nanda.governance.domain.dto.CrfResponseVO;
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

@Api(tags = "治理-CRF表单")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/governance/crf/forms")
@RequiredArgsConstructor
public class CrfFormController {

    private final CrfFormService crfFormService;

    @ApiOperation("CRF表单列表")
    @GetMapping
    @RequiresPermission("governance:crf:design")
    public Result<PageResult<CrfFormVO>> list(PageQuery query) {
        return Result.ok(crfFormService.listForms(query));
    }

    @ApiOperation("创建CRF表单")
    @PostMapping
    @RequiresPermission("governance:crf:design")
    public Result<CrfFormVO> create(@Valid @RequestBody CrfFormCreateRequest request) {
        return Result.ok(crfFormService.createForm(request));
    }

    @ApiOperation("发布CRF表单")
    @PostMapping("/{id}/publish")
    @RequiresPermission("governance:crf:design")
    public Result<CrfFormVO> publish(@PathVariable Long id) {
        return Result.ok(crfFormService.publishForm(id));
    }

    @ApiOperation("提交CRF答卷")
    @PostMapping("/responses")
    @RequiresPermission("governance:crf:entry")
    public Result<CrfResponseVO> submit(@Valid @RequestBody CrfResponseSubmitRequest request) {
        return Result.ok(crfFormService.submitResponse(request));
    }

    @ApiOperation("CRF答卷列表")
    @GetMapping("/responses")
    @RequiresPermission("governance:crf:entry")
    public Result<PageResult<CrfResponseVO>> listResponses(PageQuery query,
                                                             @RequestParam(required = false) Long formId) {
        return Result.ok(crfFormService.listResponses(query, formId));
    }
}
