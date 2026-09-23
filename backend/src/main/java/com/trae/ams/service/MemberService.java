package com.trae.ams.service;

import com.trae.ams.entity.SysUser;
import java.math.BigDecimal;
import java.util.Map;

public interface MemberService {
    /**
     * Accumulate consumption and check for upgrade
     */
    void accumulateConsumption(Long userId, BigDecimal amount);

    /**
     * Calculate discounted price
     */
    BigDecimal calculatePrice(Long userId, BigDecimal originalPrice);
    
    /**
     * Get member info for dashboard
     */
    Map<String, Object> getMemberInfo(Long userId);
    
    /**
     * Get all level rules
     */
    Object getLevelRules();
}
