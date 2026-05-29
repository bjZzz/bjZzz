package com.nanda.integration.fhir;

import com.nanda.common.audit.AuditLog;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.integration.service.IntegrationOrgContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "集成-FHIR R4")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/integration/fhir")
@RequiredArgsConstructor
public class FhirController {

    private final FhirResourceProvider fhirResourceProvider;

    @ApiOperation("读取 FHIR R4 Patient")
    @GetMapping("/Patient/{id}")
    @AuditLog(action = "FHIR_READ", resourceType = "Patient")
    @RequiresPermission("integration:fhir:read")
    public Result<Map<String, Object>> patient(
            @PathVariable("id") Long empiId,
            @RequestHeader(value = CommonConstants.HEADER_ORG_ID, required = false) Long orgId,
            @RequestHeader(value = "X-Integration-Key", required = false) String apiKey) {
        return Result.ok(fhirResourceProvider.patient(empiId, resolveOrgId(orgId), apiKey));
    }

    @ApiOperation("按 Patient 查询 FHIR R4 Observation")
    @GetMapping("/Observation")
    @AuditLog(action = "FHIR_READ", resourceType = "Observation")
    @RequiresPermission("integration:fhir:read")
    public Result<Map<String, Object>> observations(
            @RequestParam("patient") Long empiId,
            @RequestHeader(value = CommonConstants.HEADER_ORG_ID, required = false) Long orgId,
            @RequestHeader(value = "X-Integration-Key", required = false) String apiKey) {
        return Result.ok(fhirResourceProvider.observations(empiId, resolveOrgId(orgId), apiKey));
    }

    private Long resolveOrgId(Long headerOrgId) {
        return IntegrationOrgContext.resolveOrgId(headerOrgId);
    }
}
