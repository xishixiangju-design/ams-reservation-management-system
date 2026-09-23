package com.trae.ams.mapper;

import com.trae.ams.entity.AmsService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AmsServiceMapper {
    /**
     * 根据ID批量查询
     */
    List<AmsService> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID查询
     */
    AmsService selectById(Long id);

    /**
     * 查询列表
     */
    List<AmsService> selectList(@Param("storeId") Long storeId, @Param("status") Integer status);

    /**
     * 新增
     */
    int insert(AmsService service);

    /**
     * 更新
     */
    int update(AmsService service);

    /**
     * 根据ID删除
     */
    int deleteById(Long id);
}
