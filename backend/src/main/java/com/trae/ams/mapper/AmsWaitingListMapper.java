package com.trae.ams.mapper;

import com.trae.ams.dto.waitinglist.WaitingListDTO;
import com.trae.ams.dto.waitinglist.WaitingListQuery;
import com.trae.ams.entity.AmsWaitingList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AmsWaitingListMapper {
    /**
     * 统计指定状态的候补数量
     */
    int countByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") String status);

    int insert(AmsWaitingList waitingList);

    int update(AmsWaitingList waitingList);

    AmsWaitingList selectById(@Param("id") Long id);

    List<AmsWaitingList> selectByCustomerId(@Param("customerId") Long customerId);

    List<WaitingListDTO> selectDtoByCustomerId(@Param("customerId") Long customerId);

    /**
     * 查询某天的候补列表 (用于匹配空闲资源)
     */
    List<AmsWaitingList> selectByDateAndStatus(@Param("storeId") Long storeId, 
                                              @Param("date") LocalDate date, 
                                              @Param("status") String status);

    /**
     * 管理端列表查询
     */
    List<WaitingListDTO> selectList(@Param("storeId") Long storeId, @Param("query") WaitingListQuery query);
}
