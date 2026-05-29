package com.nanda.asset.controller;

import com.nanda.asset.domain.dto.AssetDtos.DualScreenSupplementRequest;
import com.nanda.asset.domain.dto.AssetDtos.SupplementResultVO;
import com.nanda.asset.supplement.SupplementService;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "资产-双屏补录")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/quality/supplement")
@RequiredArgsConstructor
public class SupplementController {

    private final SupplementService supplementService;

    @ApiOperation("双屏补录保存")
    @PostMapping("/dual-screen")
    @RequiresPermission("asset:supplement:write")
    public Result<SupplementResultVO> dualScreen(@Valid @RequestBody DualScreenSupplementRequest request) {
        return Result.ok(supplementService.dualScreenSave(request));
    }
}
