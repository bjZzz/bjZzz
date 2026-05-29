package com.nanda.integration.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.integration.config.EndpointConfigService;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.EndpointCreateRequest;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.EndpointVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "集成-端点配置")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/integration/endpoints")
@RequiredArgsConstructor
public class EndpointConfigController {

    private final EndpointConfigService endpointConfigService;

    @ApiOperation("集成端点列表")
    @GetMapping
    @RequiresPermission("integration:endpoint:manage")
    public Result<List<EndpointVO>> list(@RequestParam(required = false) String endpointType) {
        return Result.ok(endpointConfigService.list(endpointType));
    }

    @ApiOperation("创建集成端点")
    @PostMapping
    @RequiresPermission("integration:endpoint:manage")
    public Result<EndpointVO> create(@RequestBody EndpointCreateRequest request) {
        return Result.ok(endpointConfigService.create(request));
    }
}
