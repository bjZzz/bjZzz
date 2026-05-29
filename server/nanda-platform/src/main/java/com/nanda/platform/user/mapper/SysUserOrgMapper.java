package com.nanda.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.platform.user.domain.entity.SysUserOrg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserOrgMapper extends BaseMapper<SysUserOrg> {

    @Select("SELECT org_id FROM sys_user_org WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectOrgIdsByUserId(Long userId);
}
