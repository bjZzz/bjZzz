package com.nanda.platform.org.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.util.IdGenerator;
import com.nanda.platform.org.domain.dto.OrgCreateRequest;
import com.nanda.platform.org.domain.dto.OrgTreeNode;
import com.nanda.platform.org.domain.dto.OrgUpdateRequest;
import com.nanda.platform.org.domain.dto.OrgVO;
import com.nanda.platform.org.domain.entity.SysOrg;
import com.nanda.platform.org.mapper.SysOrgMapper;
import com.nanda.platform.user.domain.entity.SysUser;
import com.nanda.platform.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrgService {

    private final SysOrgMapper sysOrgMapper;
    private final SysUserMapper sysUserMapper;

    public List<OrgTreeNode> getTree() {
        List<SysOrg> all = sysOrgMapper.selectList(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getDeleted, 0)
                .orderByAsc(SysOrg::getId));
        Map<Long, OrgTreeNode> map = new HashMap<Long, OrgTreeNode>();
        List<OrgTreeNode> roots = new ArrayList<OrgTreeNode>();
        for (SysOrg org : all) {
            OrgTreeNode node = toTreeNode(org);
            map.put(org.getId(), node);
        }
        for (SysOrg org : all) {
            OrgTreeNode node = map.get(org.getId());
            if (org.getParentId() == null || !map.containsKey(org.getParentId())) {
                roots.add(node);
            } else {
                map.get(org.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }

    public OrgVO getById(Long id) {
        return toVO(requireOrg(id));
    }

    @Transactional
    public OrgVO create(OrgCreateRequest request) {
        ensureOrgCodeUnique(request.getOrgCode(), null);
        if (request.getParentId() != null) {
            requireOrg(request.getParentId());
        }
        AuthContext ctx = AuthContextHolder.get();
        SysOrg org = new SysOrg();
        org.setId(IdGenerator.nextId());
        org.setOrgCode(request.getOrgCode());
        org.setOrgName(request.getOrgName());
        org.setOrgType(request.getOrgType());
        org.setParentId(request.getParentId());
        org.setLevelType(request.getLevelType());
        org.setStatus("ACTIVE");
        org.setOrgId(org.getId());
        org.setCreatedBy(ctx != null ? ctx.getUserId() : null);
        org.setCreatedAt(LocalDateTime.now());
        org.setUpdatedAt(LocalDateTime.now());
        sysOrgMapper.insert(org);
        return toVO(org);
    }

    @Transactional
    public OrgVO update(Long id, OrgUpdateRequest request) {
        SysOrg org = requireOrg(id);
        if (request.getParentId() != null && !request.getParentId().equals(org.getParentId())) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException(ErrorCode.ORG_CYCLE_REFERENCE, "不能将机构设为自己的上级");
            }
            requireOrg(request.getParentId());
            if (wouldCreateCycle(id, request.getParentId())) {
                throw new BusinessException(ErrorCode.ORG_CYCLE_REFERENCE, "机构层级存在循环引用");
            }
            org.setParentId(request.getParentId());
        }
        if (StringUtils.hasText(request.getOrgName())) {
            org.setOrgName(request.getOrgName());
        }
        if (StringUtils.hasText(request.getOrgType())) {
            org.setOrgType(request.getOrgType());
        }
        if (StringUtils.hasText(request.getLevelType())) {
            org.setLevelType(request.getLevelType());
        }
        if (StringUtils.hasText(request.getStatus())) {
            org.setStatus(request.getStatus());
        }
        org.setUpdatedBy(AuthContextHolder.get() != null ? AuthContextHolder.get().getUserId() : null);
        org.setUpdatedAt(LocalDateTime.now());
        sysOrgMapper.updateById(org);
        return toVO(org);
    }

    @Transactional
    public void delete(Long id) {
        requireOrg(id);
        long childCount = sysOrgMapper.selectCount(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getParentId, id)
                .eq(SysOrg::getDeleted, 0));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.ORG_HAS_DEPENDENCY, "存在下级机构，无法删除");
        }
        long userCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .and(w -> w.eq(SysUser::getPrimaryOrgId, id).or().eq(SysUser::getOrgId, id))
                .eq(SysUser::getDeleted, 0));
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.ORG_HAS_DEPENDENCY, "存在绑定用户，无法删除");
        }
        sysOrgMapper.deleteById(id);
    }

    private void ensureOrgCodeUnique(String orgCode, Long excludeId) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getOrgCode, orgCode)
                .eq(SysOrg::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(SysOrg::getId, excludeId);
        }
        if (sysOrgMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.ORG_CODE_DUPLICATE, "机构编码已存在");
        }
    }

    private boolean wouldCreateCycle(Long orgId, Long newParentId) {
        Long current = newParentId;
        while (current != null) {
            if (current.equals(orgId)) {
                return true;
            }
            SysOrg parent = sysOrgMapper.selectById(current);
            if (parent == null || parent.getDeleted() != null && parent.getDeleted() == 1) {
                break;
            }
            current = parent.getParentId();
        }
        return false;
    }

    private SysOrg requireOrg(Long id) {
        SysOrg org = sysOrgMapper.selectById(id);
        if (org == null || org.getDeleted() != null && org.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "机构不存在");
        }
        return org;
    }

    private OrgTreeNode toTreeNode(SysOrg org) {
        OrgTreeNode node = new OrgTreeNode();
        node.setId(org.getId());
        node.setOrgCode(org.getOrgCode());
        node.setOrgName(org.getOrgName());
        node.setParentId(org.getParentId());
        return node;
    }

    private OrgVO toVO(SysOrg org) {
        OrgVO vo = new OrgVO();
        vo.setId(org.getId());
        vo.setOrgCode(org.getOrgCode());
        vo.setOrgName(org.getOrgName());
        vo.setOrgType(org.getOrgType());
        vo.setParentId(org.getParentId());
        vo.setLevelType(org.getLevelType());
        vo.setStatus(org.getStatus());
        vo.setCreatedAt(org.getCreatedAt());
        return vo;
    }
}
