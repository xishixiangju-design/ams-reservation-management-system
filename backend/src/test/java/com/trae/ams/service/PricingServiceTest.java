package com.trae.ams.service;

import com.trae.ams.entity.AmsPricingRule;
import com.trae.ams.mapper.AmsPricingRuleMapper;
import com.trae.ams.mapper.AmsServiceMapper;
import com.trae.ams.service.impl.PricingServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.trae.ams.common.context.UserContext;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class PricingServiceTest {

    @InjectMocks
    private PricingServiceImpl pricingService;

    @Mock
    private AmsPricingRuleMapper pricingRuleMapper;

    @Mock
    private AmsServiceMapper serviceMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCalculatePrice_NoRules() {
        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getStoreIdOrDefault).thenReturn(1L);
            
            BigDecimal unitPrice = new BigDecimal("100");
            when(pricingRuleMapper.selectRulesByService(any(), any())).thenReturn(Collections.emptyList());

            BigDecimal result = pricingService.calculateServicePrice(1L, 5, unitPrice);
            // 100 * 5 = 500
            Assertions.assertEquals(0, new BigDecimal("500").compareTo(result));
        }
    }

    @Test
    public void testCalculatePrice_Tiered_Override() {
        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getStoreIdOrDefault).thenReturn(1L);

            // Rule: 5-10 people -> $80 per head
            AmsPricingRule rule = new AmsPricingRule();
            rule.setRuleType("TIERED");
            rule.setMinPeople(5);
            rule.setMaxPeople(10);
            rule.setAdjustmentType("PRICE_OVERRIDE");
            rule.setAdjustmentValue(new BigDecimal("80"));

            when(pricingRuleMapper.selectRulesByService(any(), any())).thenReturn(Collections.singletonList(rule));

            BigDecimal result = pricingService.calculateServicePrice(1L, 5, new BigDecimal("100"));
            // 80 * 5 = 400
            Assertions.assertEquals(0, new BigDecimal("400").compareTo(result));
        }
    }

    @Test
    public void testCalculatePrice_Group_Discount() {
        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getStoreIdOrDefault).thenReturn(1L);

            // Rule: 10+ people -> Total Price - $50
            AmsPricingRule rule = new AmsPricingRule();
            rule.setRuleType("GROUP");
            rule.setMinPeople(10);
            rule.setMaxPeople(20);
            rule.setAdjustmentType("DISCOUNT_AMOUNT");
            rule.setAdjustmentValue(new BigDecimal("50"));

            when(pricingRuleMapper.selectRulesByService(any(), any())).thenReturn(Collections.singletonList(rule));

            BigDecimal result = pricingService.calculateServicePrice(1L, 10, new BigDecimal("100"));
            // Original Total: 100 * 10 = 1000.
            // Discounted: 1000 - 50 = 950.
            Assertions.assertEquals(0, new BigDecimal("950").compareTo(result));
        }
    }
}
