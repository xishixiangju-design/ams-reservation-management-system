package com.trae.ams.mapper;

import com.trae.ams.entity.AmsAppointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AmsAppointmentMapper {
    /**
     * 根据ID查询 (包含客户信息)
     */
    AmsAppointment selectById(Long id);

    /**
     * 插入预约
     */
    int insert(AmsAppointment appointment);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新退款状态
     */
    int updateRefundStatus(@Param("id") Long id, @Param("refundStatus") Integer refundStatus);

    /**
     * 更新预约信息
     */
    int update(AmsAppointment appointment);

    /**
     * 查询冲突的预约 (用于Slot计算)
     * 查找在 [start, end] 范围内有交集的预约
     * 且状态不是已取消(3)或违约(4)
     */
    List<AmsAppointment> selectConflictAppointments(
            @Param("storeId") Long storeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 用户历史预约 (带筛选)
     */
    List<AmsAppointment> selectByCustomer(@Param("customerId") Long customerId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("status") Integer status);

    /**
     * 管理端查询列表
     */
    List<AmsAppointment> selectList(@Param("storeId") Long storeId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("status") Integer status,
                                    @Param("keyword") String keyword);

    // Dashboard Statistics
    int countByDate(@Param("storeId") Long storeId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    java.math.BigDecimal sumTotalAmountByDateAndStatus(@Param("storeId") Long storeId,
                                                       @Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end,
                                                       @Param("statuses") List<Integer> statuses);

    java.math.BigDecimal sumPaidAmountByDate(@Param("storeId") Long storeId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    int countByDateAndStatusList(@Param("storeId") Long storeId,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("statuses") List<Integer> statuses);

    int countByDateAndStatus(@Param("storeId") Long storeId,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("status") Integer status);

    List<AmsAppointment> selectActiveAppointments(@Param("storeId") Long storeId, @Param("now") LocalDateTime now);

    /**
     * 技师预约列表 (带筛选)
     */
    List<AmsAppointment> selectByTechnician(@Param("techId") Long techId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            @Param("status") Integer status);

    List<java.util.Map<String, Object>> selectCalendarStats(@Param("storeId") Long storeId,
                                                           @Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end);
}
