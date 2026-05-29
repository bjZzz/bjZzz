package com.nanda.asset.controller;

import com.nanda.asset.domain.dto.AssetDtos.KnowledgeDocumentVO;
import com.nanda.asset.domain.dto.AssetDtos.KnowledgeImportRequest;
import com.nanda.asset.knowledge.KnowledgeDocumentService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "资产-知识库")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeDocumentService knowledgeDocumentService;

    @ApiOperation("知识库检索")
    @GetMapping("/documents")
    @RequiresPermission("asset:knowledge:read")
    public Result<PageResult<KnowledgeDocumentVO>> search(PageQuery query,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) String tag) {
        return Result.ok(knowledgeDocumentService.search(query, keyword, tag));
    }

    @ApiOperation("知识库文档详情")
    @GetMapping("/documents/{id}")
    @RequiresPermission("asset:knowledge:read")
    public Result<KnowledgeDocumentVO> getDocument(@PathVariable Long id) {
        return Result.ok(knowledgeDocumentService.getDocument(id));
    }

    @ApiOperation("导入知识库文档")
    @PostMapping("/documents")
    @RequiresPermission("asset:knowledge:write")
    public Result<KnowledgeDocumentVO> importDocument(@Valid @RequestBody KnowledgeImportRequest request) {
        return Result.ok(knowledgeDocumentService.importDocument(request));
    }
}
