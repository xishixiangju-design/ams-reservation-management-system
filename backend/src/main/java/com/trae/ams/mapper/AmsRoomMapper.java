package com.trae.ams.mapper;

import com.trae.ams.entity.AmsRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AmsRoomMapper {
    /**
     * 根据ID批量查询
     */
    List<AmsRoom> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID查询
     */
    AmsRoom selectById(Long id);

    /**
     * 查询列表
     */
    List<AmsRoom> selectList(@Param("storeId") Long storeId, @Param("status") Integer status);

    /**
     * 新增
     */
    int insert(AmsRoom room);

    /**
     * 更新
     */
    int update(AmsRoom room);

    /**
     * 删除
     */
    int deleteById(Long id);
}
