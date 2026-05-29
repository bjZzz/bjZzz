package com.nanda.platform.audit.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.platform.audit.domain.dto.AuditLogVO;
import com.nanda.platform.audit.service.AuditLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "审计日志")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/audit/logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @ApiOperation("审计日志列表")
    @GetMapping
    @RequiresPermission("platform:audit:read")
    public Result<PageResult<AuditLogVO>> list(PageQuery query,
                                               @RequestParam(required = false) Long userId,
                                               @RequestParam(required = false) String dateFrom,
                                               @RequestParam(required = false) String dateTo) {
        return Result.ok(auditLogService.list(query, userId, dateFrom, dateTo));
    }
}
