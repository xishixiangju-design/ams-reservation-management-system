package com.trae.ams.mapper;

import com.trae.ams.entity.SysNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysNotificationMapper {
    int insert(SysNotification notification);
    int update(SysNotification notification);
    
    List<SysNotification> selectByUserId(@Param("userId") Long userId);
    List<SysNotification> selectByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    long countUnreadByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    int updateReadStatus(@Param("id") Long id, @Param("readStatus") Integer readStatus);
    int markAllAsRead(@Param("userId") Long userId);
    int deleteById(@Param("id") Long id);
    SysNotification selectById(@Param("id") Long id);
}
