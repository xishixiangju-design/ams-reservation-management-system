package com.trae.ams.mapper;

import com.trae.ams.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {
    int insert(SysUserRole sysUserRole);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    int deleteByUserId(@Param("userId") Long userId);
}
