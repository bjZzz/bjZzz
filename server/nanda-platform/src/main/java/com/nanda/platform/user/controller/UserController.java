package com.nanda.platform.user.controller;

import com.nanda.common.audit.AuditLog;
import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.platform.user.domain.dto.AssignRolesRequest;
import com.nanda.platform.user.domain.dto.BindOrgsRequest;
import com.nanda.platform.user.domain.dto.UpdateUserStatusRequest;
import com.nanda.platform.user.domain.dto.UserCreateRequest;
import com.nanda.platform.user.domain.dto.UserUpdateRequest;
import com.nanda.platform.user.domain.dto.UserVO;
import com.nanda.platform.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "用户")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ApiOperation("用户列表")
    @GetMapping
    @RequiresPermission("platform:user:read")
    public Result<PageResult<UserVO>> list(PageQuery query,
                                          @RequestParam(required = false) String username,
                                          @RequestParam(required = false) String status) {
        return Result.ok(userService.list(query, username, status));
    }

    @ApiOperation("用户详情")
    @GetMapping("/{id}")
    @RequiresPermission("platform:user:read")
    public Result<UserVO> get(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @ApiOperation("创建用户")
    @PostMapping
    @RequiresPermission("platform:user:write")
    @AuditLog(action = "CREATE", resourceType = "user")
    public Result<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.ok(userService.create(request));
    }

    @ApiOperation("更新用户")
    @PutMapping("/{id}")
    @RequiresPermission("platform:user:write")
    @AuditLog(action = "UPDATE", resourceType = "user")
    public Result<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return Result.ok(userService.update(id, request));
    }

    @ApiOperation("分配角色")
    @PutMapping("/{id}/roles")
    @RequiresPermission("platform:user:write")
    @AuditLog(action = "ASSIGN_ROLES", resourceType = "user")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request);
        return Result.ok(null);
    }

    @ApiOperation("绑定机构")
    @PutMapping("/{id}/orgs")
    @RequiresPermission("platform:user:write")
    @AuditLog(action = "BIND_ORGS", resourceType = "user")
    public Result<Void> bindOrgs(@PathVariable Long id, @Valid @RequestBody BindOrgsRequest request) {
        userService.bindOrgs(id, request);
        return Result.ok(null);
    }

    @ApiOperation("更新用户状态")
    @PutMapping("/{id}/status")
    @RequiresPermission("platform:user:write")
    @AuditLog(action = "UPDATE_STATUS", resourceType = "user")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        userService.updateStatus(id, request.getStatus());
        return Result.ok(null);
    }
}
