package com.trae.ams.service;

import java.math.BigDecimal;

public interface PricingService {
    /**
     * 计算服务价格
     * @param serviceId 服务ID
     * @param peopleCount 人数
     * @param originalPrice 原始单价 (如果为null，内部查询)
     * @return 计算后的总价
     */
    BigDecimal calculateServicePrice(Long serviceId, int peopleCount, BigDecimal originalPrice);
}
