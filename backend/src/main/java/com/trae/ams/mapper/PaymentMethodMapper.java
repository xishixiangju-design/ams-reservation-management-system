package com.trae.ams.mapper;

import com.trae.ams.entity.PaymentMethod;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PaymentMethodMapper {
    List<PaymentMethod> selectAll();
    List<PaymentMethod> selectActive();
    int insert(PaymentMethod paymentMethod);
    int update(PaymentMethod paymentMethod);
    PaymentMethod selectById(Long id);
}
