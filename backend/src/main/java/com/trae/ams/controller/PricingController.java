package com.trae.ams.controller;

import com.trae.ams.common.result.Result;
import com.trae.ams.dto.PricingRequest;
import com.trae.ams.entity.AmsService;
import com.trae.ams.mapper.AmsServiceMapper;
import com.trae.ams.service.MemberService;
import com.trae.ams.service.PricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private static final Logger log = LoggerFactory.getLogger(PricingController.class);

    @Autowired
    private PricingService pricingService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AmsServiceMapper serviceMapper;

    @PostMapping("/calculate")
    public Result<BigDecimal> calculate(@RequestBody PricingRequest request) {
        try {
            // 1. Get Base Price (Member Discounted)
            AmsService service = serviceMapper.selectById(request.getServiceId());
            if (service == null) {
                return Result.error("Service not found");
            }
            
            Long customerId = request.getCustomerId();
            if (customerId == null) {
                customerId = com.trae.ams.common.context.UserContext.getUserId();
            }
            
            // Calculate member price (unit price)
            BigDecimal memberPrice = memberService.calculatePrice(customerId, service.getPrice());
            
            // Apply Pricing Rules
            int peopleCount = request.getPeopleCount() != null ? request.getPeopleCount() : 1;
            BigDecimal total = pricingService.calculateServicePrice(request.getServiceId(), peopleCount, memberPrice);
            
            log.info("Pricing Calc: User={}, Service={}, People={}, Result={}", customerId, request.getServiceId(), peopleCount, total);
    
            return Result.success(total);
        } catch (Exception e) {
            log.error("Pricing calculation failed", e);
            return Result.error("Calculation failed: " + e.getMessage());
        }
    }
}
