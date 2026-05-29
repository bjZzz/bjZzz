package com.nanda.platform.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.platform.org.domain.entity.SysOrg;
import com.nanda.platform.org.mapper.SysOrgMapper;
import com.nanda.platform.user.mapper.SysUserOrgMapper;
import com.nanda.platform.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DataPermissionService {

    private final SysOrgMapper sysOrgMapper;
    private final SysUserOrgMapper sysUserOrgMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public Set<Long> resolveAccessibleOrgIds(Long userId, Long currentOrgId) {
        List<String> dataScopes = sysUserRoleMapper.selectDataScopesByUserId(userId);
        if (dataScopes.isEmpty()) {
            return currentOrgId != null
                    ? Collections.singleton(currentOrgId)
                    : Collections.<Long>emptySet();
        }
        if (dataScopes.contains("ALL")) {
            return loadAllOrgIds();
        }
        Set<Long> orgIds = new HashSet<Long>();
        if (dataScopes.contains("ORG") || dataScopes.contains("ORG_AND_CHILD")) {
            if (currentOrgId != null) {
                orgIds.add(currentOrgId);
                if (dataScopes.contains("ORG_AND_CHILD")) {
                    orgIds.addAll(collectDescendantOrgIds(currentOrgId));
                }
            }
        }
        if (dataScopes.contains("CUSTOM")) {
            orgIds.addAll(sysUserOrgMapper.selectOrgIdsByUserId(userId));
        }
        if (orgIds.isEmpty() && currentOrgId != null) {
            orgIds.add(currentOrgId);
        }
        return orgIds;
    }

    public boolean canAccessOrg(Long userId, Long currentOrgId, Long targetOrgId) {
        if (targetOrgId == null) {
            return true;
        }
        return resolveAccessibleOrgIds(userId, currentOrgId).contains(targetOrgId);
    }

    private Set<Long> loadAllOrgIds() {
        List<SysOrg> orgs = sysOrgMapper.selectList(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getDeleted, 0)
                .select(SysOrg::getId));
        Set<Long> ids = new HashSet<Long>();
        for (SysOrg org : orgs) {
            ids.add(org.getId());
        }
        return ids;
    }

    private List<Long> collectDescendantOrgIds(Long rootOrgId) {
        List<SysOrg> all = sysOrgMapper.selectList(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getDeleted, 0));
        List<Long> descendants = new ArrayList<Long>();
        collectChildren(rootOrgId, all, descendants);
        return descendants;
    }

    private void collectChildren(Long parentId, List<SysOrg> all, List<Long> result) {
        for (SysOrg org : all) {
            if (parentId.equals(org.getParentId())) {
                result.add(org.getId());
                collectChildren(org.getId(), all, result);
            }
        }
    }
}
