package com.nanda.platform.user.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.platform.user.domain.dto.PermissionTreeNode;
import com.nanda.platform.user.domain.dto.RoleVO;
import com.nanda.platform.user.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "角色权限")
@RestController
@RequestMapping(CommonConstants.API_PREFIX)
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @ApiOperation("角色列表")
    @GetMapping("/roles")
    @RequiresPermission("platform:user:read")
    public Result<List<RoleVO>> listRoles() {
        return Result.ok(roleService.listRoles());
    }

    @ApiOperation("权限树")
    @GetMapping("/permissions/tree")
    @RequiresPermission("platform:user:read")
    public Result<List<PermissionTreeNode>> permissionTree() {
        return Result.ok(roleService.permissionTree());
    }
}
