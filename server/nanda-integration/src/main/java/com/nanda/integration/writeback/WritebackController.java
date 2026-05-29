package com.nanda.integration.writeback;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.audit.AuditLog;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.WritebackRequest;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.WritebackResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "集成-外部回写")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/integration/writeback")
@RequiredArgsConstructor
public class WritebackController {

    private final WritebackService writebackService;

    @ApiOperation("评估结果回写")
    @PostMapping
    @AuditLog(action = "WRITEBACK", resourceType = "integration")
    @RequiresPermission("integration:writeback:execute")
    public Result<WritebackResultVO> submit(
            @RequestBody WritebackRequest request,
            @RequestHeader(value = CommonConstants.HEADER_ORG_ID, required = false) Long orgId,
            @RequestHeader(value = CommonConstants.HEADER_REQUEST_ID, required = false) String requestId,
            @RequestHeader(value = "X-Integration-Key", required = false) String apiKey) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "请求体不能为空");
        }
        if (!StringUtils.hasText(request.getClientRequestId()) && StringUtils.hasText(requestId)) {
            request.setClientRequestId(requestId);
        }
        return Result.ok(writebackService.submit(request, orgId, apiKey));
    }
}
