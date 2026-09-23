package com.trae.ams.mapper;

import com.trae.ams.entity.AmsTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AmsTransactionMapper {
    int insert(AmsTransaction transaction);
    
    AmsTransaction selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);
    
    AmsTransaction selectSuccessPaymentByAppointmentId(@Param("appointmentId") Long appointmentId);
    
    int updateStatus(@Param("outTradeNo") String outTradeNo, 
                    @Param("status") String status, 
                    @Param("tradeNo") String tradeNo,
                    @Param("paymentMethod") String paymentMethod,
                    @Param("payTime") java.time.LocalDateTime payTime);
}
