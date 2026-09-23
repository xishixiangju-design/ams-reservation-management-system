package com.trae.ams.mapper;

import com.trae.ams.dto.technician.TechnicianConflictDTO;
import com.trae.ams.entity.AmsAppointmentItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AmsAppointmentItemMapper {
    /**
     * 批量插入
     */
    int batchInsert(@Param("list") List<AmsAppointmentItem> list);

    /**
     * 根据主订单ID批量查询
     */
    List<AmsAppointmentItem> selectByApptIds(@Param("apptIds") List<Long> apptIds);

    /**
     * 根据主订单ID查询
     */
    List<AmsAppointmentItem> selectByApptId(Long apptId);
    
    /**
     * 根据主订单ID删除
     */
    int deleteByApptId(Long apptId);
    
    /**
     * 查询指定技师在某时间之后的订单明细
     */
    List<AmsAppointmentItem> selectFutureItemsByTechId(@Param("techId") Long techId, @Param("minTime") LocalDateTime minTime);

    /**
     * 查询指定技师在某时间之后的冲突详情
     */
    List<TechnicianConflictDTO> selectConflictDetailsByTechId(@Param("techId") Long techId, @Param("minTime") LocalDateTime minTime);

    /**
     * 统计指定技师的进行中订单数 (status IN (0, 1))
     */
    int countActiveByTechId(@Param("techId") Long techId);

    /**
     * 统计指定技师的历史总订单数
     */
    int countTotalByTechId(@Param("techId") Long techId);

    /**
     * 统计使用该服务的订单数
     */
    int countByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 统计使用该房间的订单数
     */
    int countByRoomId(@Param("roomId") Long roomId);
}
