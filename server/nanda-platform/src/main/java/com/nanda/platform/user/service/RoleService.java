package com.nanda.platform.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.platform.user.domain.dto.PermissionTreeNode;
import com.nanda.platform.user.domain.dto.RoleVO;
import com.nanda.platform.user.domain.entity.SysPermission;
import com.nanda.platform.user.domain.entity.SysRole;
import com.nanda.platform.user.mapper.SysPermissionMapper;
import com.nanda.platform.user.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public List<RoleVO> listRoles() {
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .orderByAsc(SysRole::getId));
        List<RoleVO> result = new ArrayList<RoleVO>();
        for (SysRole role : roles) {
            RoleVO vo = new RoleVO();
            vo.setId(role.getId());
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
            vo.setDataScope(role.getDataScope());
            result.add(vo);
        }
        return result;
    }

    public List<PermissionTreeNode> permissionTree() {
        List<SysPermission> permissions = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getDeleted, 0)
                .orderByAsc(SysPermission::getModule, SysPermission::getId));
        Map<String, PermissionTreeNode> moduleMap = new HashMap<String, PermissionTreeNode>();
        List<PermissionTreeNode> roots = new ArrayList<PermissionTreeNode>();
        for (SysPermission perm : permissions) {
            String module = perm.getModule() != null ? perm.getModule() : "other";
            PermissionTreeNode moduleNode = moduleMap.get(module);
            if (moduleNode == null) {
                moduleNode = new PermissionTreeNode();
                moduleNode.setPermCode(module);
                moduleNode.setPermName(module);
                moduleNode.setModule(module);
                moduleMap.put(module, moduleNode);
                roots.add(moduleNode);
            }
            PermissionTreeNode leaf = new PermissionTreeNode();
            leaf.setId(perm.getId());
            leaf.setPermCode(perm.getPermCode());
            leaf.setPermName(perm.getPermName());
            leaf.setModule(perm.getModule());
            moduleNode.getChildren().add(leaf);
        }
        return roots;
    }
}
