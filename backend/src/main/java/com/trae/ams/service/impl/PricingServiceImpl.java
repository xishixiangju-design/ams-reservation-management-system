package com.trae.ams.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.entity.AmsPricingRule;
import com.trae.ams.entity.AmsService;
import com.trae.ams.mapper.AmsPricingRuleMapper;
import com.trae.ams.mapper.AmsServiceMapper;
import com.trae.ams.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingServiceImpl implements PricingService {

    @Autowired
    private AmsPricingRuleMapper pricingRuleMapper;

    @Autowired
    private AmsServiceMapper serviceMapper;

    @Override
    public BigDecimal calculateServicePrice(Long serviceId, int peopleCount, BigDecimal originalPrice) {
        if (originalPrice == null) {
            AmsService service = serviceMapper.selectById(serviceId);
            if (service == null) return BigDecimal.ZERO;
            originalPrice = service.getPrice();
        }

        // 默认按人头计费: 单价 * 人数
        BigDecimal baseTotal = originalPrice.multiply(new BigDecimal(peopleCount));

        // 查询规则
        Long storeId = UserContext.getStoreIdOrDefault();
        List<AmsPricingRule> rules = pricingRuleMapper.selectRulesByService(storeId, serviceId);

        if (CollUtil.isEmpty(rules)) {
            return baseTotal;
        }

        // 过滤适用人数范围的规则
        // 规则优先级排序已经在SQL中完成 (Priority DESC, MinPeople ASC)
        // 我们取第一个匹配的高优先级规则
        for (AmsPricingRule rule : rules) {
            if (peopleCount >= rule.getMinPeople() && peopleCount <= rule.getMaxPeople()) {
                return applyRule(baseTotal, originalPrice, peopleCount, rule);
            }
        }

        return baseTotal;
    }

    private BigDecimal applyRule(BigDecimal currentTotal, BigDecimal unitPrice, int peopleCount, AmsPricingRule rule) {
        BigDecimal result = currentTotal;

        // TIERED: 针对单价调整，再乘人数
        if ("TIERED".equalsIgnoreCase(rule.getRuleType())) {
            BigDecimal newUnit = unitPrice;
            if ("PRICE_OVERRIDE".equalsIgnoreCase(rule.getAdjustmentType())) {
                newUnit = rule.getAdjustmentValue();
            } else if ("DISCOUNT_RATE".equalsIgnoreCase(rule.getAdjustmentType())) {
                newUnit = unitPrice.multiply(rule.getAdjustmentValue());
            } else if ("DISCOUNT_AMOUNT".equalsIgnoreCase(rule.getAdjustmentType())) {
                newUnit = unitPrice.subtract(rule.getAdjustmentValue());
            }
            result = newUnit.multiply(new BigDecimal(peopleCount));
        } 
        // GROUP: 针对总价调整
        else if ("GROUP".equalsIgnoreCase(rule.getRuleType())) {
            if ("PRICE_OVERRIDE".equalsIgnoreCase(rule.getAdjustmentType())) {
                result = rule.getAdjustmentValue();
            } else if ("DISCOUNT_RATE".equalsIgnoreCase(rule.getAdjustmentType())) {
                result = currentTotal.multiply(rule.getAdjustmentValue());
            } else if ("DISCOUNT_AMOUNT".equalsIgnoreCase(rule.getAdjustmentType())) {
                result = currentTotal.subtract(rule.getAdjustmentValue());
            }
        }
        
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }
}
