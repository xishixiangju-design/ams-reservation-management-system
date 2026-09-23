package com.trae.ams.mapper;

import com.trae.ams.entity.AmsTechnicianShift;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AmsTechnicianShiftMapper {
    int insert(AmsTechnicianShift shift);

    int update(AmsTechnicianShift shift);

    int deleteById(@Param("id") Long id);

    /**
     * 删除指定技师指定日期的排班 (用于重置)
     */
    int deleteByTechAndDate(@Param("techId") Long techId, @Param("date") LocalDate date);

    /**
     * 查询指定技师指定日期的排班
     */
    List<AmsTechnicianShift> selectByTechAndDate(@Param("techId") Long techId, @Param("date") LocalDate date);

    /**
     * 查询指定日期范围的排班
     */
    List<AmsTechnicianShift> selectByDateRange(@Param("techId") Long techId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    /**
     * 查询某天所有技师的排班
     */
    List<AmsTechnicianShift> selectAllByDate(@Param("date") LocalDate date);
}
