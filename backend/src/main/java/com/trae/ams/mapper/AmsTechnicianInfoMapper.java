package com.trae.ams.mapper;

import com.trae.ams.entity.AmsTechnicianInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AmsTechnicianInfoMapper {
    /**
     * 根据UserId批量查询
     */
    List<AmsTechnicianInfo> selectByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 根据UserId查询
     */
    AmsTechnicianInfo selectByUserId(Long userId);

    /**
     * 查询列表 (关联 sys_user)
     */
    List<AmsTechnicianInfo> selectList(@Param("storeId") Long storeId, @Param("status") String status, @Param("name") String name);

    /**
     * 新增
     */
    int insert(AmsTechnicianInfo info);

    /**
     * 更新
     */
    int update(AmsTechnicianInfo info);

    /**
     * 删除
     */
    int deleteByUserId(Long userId);
}
