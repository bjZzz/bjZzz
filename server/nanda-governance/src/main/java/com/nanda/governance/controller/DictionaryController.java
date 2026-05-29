package com.nanda.governance.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.governance.dictionary.DictionaryService;
import com.nanda.governance.domain.dto.DictDiagnosisCreateRequest;
import com.nanda.governance.domain.dto.DictDiagnosisVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "治理-字典")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/governance/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @ApiOperation("诊断字典列表")
    @GetMapping("/diagnosis")
    @RequiresPermission("governance:dict:read")
    public Result<List<DictDiagnosisVO>> listDiagnosis() {
        return Result.ok(dictionaryService.listDiagnosis());
    }

    @ApiOperation("创建诊断字典项")
    @PostMapping("/diagnosis")
    @RequiresPermission("governance:dict:write")
    public Result<DictDiagnosisVO> createDiagnosis(@Valid @RequestBody DictDiagnosisCreateRequest request) {
        return Result.ok(dictionaryService.createDiagnosis(request));
    }
}
