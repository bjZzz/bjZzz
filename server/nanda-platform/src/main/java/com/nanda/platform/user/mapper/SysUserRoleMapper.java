package com.nanda.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.platform.user.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectRoleIdsByUserId(Long userId);

    @Select("SELECT r.data_scope FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id AND ur.deleted = 0 " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> selectDataScopesByUserId(Long userId);
}
