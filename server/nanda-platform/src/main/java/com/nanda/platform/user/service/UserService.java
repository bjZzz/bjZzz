package com.nanda.platform.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.util.IdGenerator;
import com.nanda.platform.user.domain.dto.AssignRolesRequest;
import com.nanda.platform.user.domain.dto.BindOrgsRequest;
import com.nanda.platform.user.domain.dto.UserCreateRequest;
import com.nanda.platform.user.domain.dto.UserUpdateRequest;
import com.nanda.platform.user.domain.dto.UserVO;
import com.nanda.platform.user.domain.entity.SysUser;
import com.nanda.platform.user.domain.entity.SysUserOrg;
import com.nanda.platform.user.domain.entity.SysUserRole;
import com.nanda.platform.user.mapper.SysUserMapper;
import com.nanda.platform.user.mapper.SysUserOrgMapper;
import com.nanda.platform.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> VALID_STATUS = new HashSet<String>(
            Arrays.asList("ENABLED", "DISABLED", "FROZEN"));

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysUserOrgMapper sysUserOrgMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<UserVO> list(PageQuery query, String username, String status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .like(StringUtils.hasText(username), SysUser::getUsername, username)
                .eq(StringUtils.hasText(status), SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> page = sysUserMapper.selectPage(new Page<SysUser>(query.getPage(), query.getSize()), wrapper);
        List<UserVO> items = new ArrayList<UserVO>();
        for (SysUser user : page.getRecords()) {
            items.add(toVO(user));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    public UserVO getById(Long id) {
        SysUser user = requireUser(id);
        return toVO(user);
    }

    @Transactional
    public UserVO create(UserCreateRequest request) {
        validatePassword(request.getPassword());
        long exists = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));
        if (exists > 0) {
            throw new BusinessException(ErrorCode.USERNAME_DUPLICATE, "用户名已存在");
        }
        AuthContext ctx = AuthContextHolder.get();
        SysUser user = new SysUser();
        user.setId(IdGenerator.nextId());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setPrimaryOrgId(request.getPrimaryOrgId());
        user.setOrgId(request.getPrimaryOrgId());
        user.setStatus("ENABLED");
        user.setCreatedBy(ctx != null ? ctx.getUserId() : null);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.insert(user);
        replaceRoles(user.getId(), request.getRoleIds());
        if (request.getOrgIds() != null && !request.getOrgIds().isEmpty()) {
            replaceOrgs(user.getId(), request.getOrgIds());
        } else if (request.getPrimaryOrgId() != null) {
            replaceOrgs(user.getId(), Arrays.asList(request.getPrimaryOrgId()));
        }
        return toVO(user);
    }

    @Transactional
    public UserVO update(Long id, UserUpdateRequest request) {
        SysUser user = requireUser(id);
        if (StringUtils.hasText(request.getDisplayName())) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getPrimaryOrgId() != null) {
            user.setPrimaryOrgId(request.getPrimaryOrgId());
            user.setOrgId(request.getPrimaryOrgId());
        }
        user.setUpdatedBy(AuthContextHolder.get() != null ? AuthContextHolder.get().getUserId() : null);
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return toVO(user);
    }

    @Transactional
    public void assignRoles(Long id, AssignRolesRequest request) {
        requireUser(id);
        replaceRoles(id, request.getRoleIds());
    }

    @Transactional
    public void bindOrgs(Long id, BindOrgsRequest request) {
        requireUser(id);
        replaceOrgs(id, request.getOrgIds());
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "无效的用户状态");
        }
        SysUser user = requireUser(id);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    private void replaceRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setId(IdGenerator.nextId());
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setDeleted(0);
            sysUserRoleMapper.insert(ur);
        }
    }

    private void replaceOrgs(Long userId, List<Long> orgIds) {
        sysUserOrgMapper.delete(new LambdaQueryWrapper<SysUserOrg>().eq(SysUserOrg::getUserId, userId));
        for (Long orgId : orgIds) {
            SysUserOrg uo = new SysUserOrg();
            uo.setId(IdGenerator.nextId());
            uo.setUserId(userId);
            uo.setOrgId(orgId);
            uo.setCreatedAt(LocalDateTime.now());
            uo.setDeleted(0);
            sysUserOrgMapper.insert(uo);
        }
    }

    private SysUser requireUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setDisplayName(user.getDisplayName());
        vo.setPrimaryOrgId(user.getPrimaryOrgId());
        vo.setStatus(user.getStatus());
        vo.setOrgId(user.getOrgId());
        vo.setRoleIds(sysUserRoleMapper.selectRoleIdsByUserId(user.getId()));
        vo.setOrgIds(sysUserOrgMapper.selectOrgIdsByUserId(user.getId()));
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "密码长度至少8位");
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "密码须包含大小写字母和数字");
        }
    }
}
