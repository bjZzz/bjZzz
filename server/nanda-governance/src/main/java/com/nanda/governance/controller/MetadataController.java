package com.nanda.governance.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.governance.domain.dto.LineageEdgeVO;
import com.nanda.governance.domain.dto.MetadataCatalogVO;
import com.nanda.governance.metadata.MetadataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "治理-元数据")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/governance/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;

    @ApiOperation("元数据目录")
    @GetMapping("/catalog")
    @RequiresPermission("governance:metadata:read")
    public Result<List<MetadataCatalogVO>> catalog() {
        return Result.ok(metadataService.listCatalog());
    }

    @ApiOperation("血缘查询")
    @GetMapping("/lineage")
    @RequiresPermission("governance:metadata:read")
    public Result<List<LineageEdgeVO>> lineage(@RequestParam(required = false) String sourceType,
                                               @RequestParam(required = false) String sourceId) {
        return Result.ok(metadataService.queryLineage(sourceType, sourceId));
    }
}
