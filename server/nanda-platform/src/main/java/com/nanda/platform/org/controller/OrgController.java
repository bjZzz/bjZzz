package com.nanda.platform.org.controller;

import com.nanda.common.audit.AuditLog;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.platform.org.domain.dto.OrgCreateRequest;
import com.nanda.platform.org.domain.dto.OrgTreeNode;
import com.nanda.platform.org.domain.dto.OrgUpdateRequest;
import com.nanda.platform.org.domain.dto.OrgVO;
import com.nanda.platform.org.service.OrgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "机构")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/orgs")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @ApiOperation("机构树")
    @GetMapping("/tree")
    @RequiresPermission("platform:org:read")
    public Result<List<OrgTreeNode>> tree() {
        return Result.ok(orgService.getTree());
    }

    @ApiOperation("机构详情")
    @GetMapping("/{id}")
    @RequiresPermission("platform:org:read")
    public Result<OrgVO> get(@PathVariable Long id) {
        return Result.ok(orgService.getById(id));
    }

    @ApiOperation("创建机构")
    @PostMapping
    @RequiresPermission("platform:org:write")
    @AuditLog(action = "CREATE", resourceType = "org")
    public Result<OrgVO> create(@Valid @RequestBody OrgCreateRequest request) {
        return Result.ok(orgService.create(request));
    }

    @ApiOperation("更新机构")
    @PutMapping("/{id}")
    @RequiresPermission("platform:org:write")
    @AuditLog(action = "UPDATE", resourceType = "org")
    public Result<OrgVO> update(@PathVariable Long id, @Valid @RequestBody OrgUpdateRequest request) {
        return Result.ok(orgService.update(id, request));
    }

    @ApiOperation("删除机构")
    @DeleteMapping("/{id}")
    @RequiresPermission("platform:org:write")
    @AuditLog(action = "DELETE", resourceType = "org")
    public Result<Void> delete(@PathVariable Long id) {
        orgService.delete(id);
        return Result.ok(null);
    }
}
