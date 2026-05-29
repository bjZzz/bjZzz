package com.nanda.boot.web;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "健康检查")
@RestController
@RequestMapping(CommonConstants.API_PREFIX)
public class HealthController {

    @ApiOperation("健康检查")
    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("status", "UP");
        data.put("application", "nanda-server");
        return Result.ok(data);
    }
}
