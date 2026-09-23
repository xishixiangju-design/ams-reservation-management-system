package com.trae.ams.controller;

import com.trae.ams.common.result.Result;
import com.trae.ams.entity.AmsTransaction;
import com.trae.ams.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/alipay/pay")
    public Result<String> pay(@RequestParam Long appointmentId) {
        String form = paymentService.initiateAlipay(appointmentId);
        return Result.success(form);
    }

    @PostMapping("/alipay/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        
        return paymentService.handleAlipayNotify(params);
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> queryStatus(@RequestParam String outTradeNo) {
        // Use syncStatusWithAlipay to actively query Alipay if local status is pending
        AmsTransaction transaction = paymentService.syncStatusWithAlipay(outTradeNo);
        
        if (transaction == null) {
            return Result.error("Transaction not found");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("status", transaction.getStatus());
        data.put("amount", transaction.getAmount());
        data.put("payTime", transaction.getPayTime());
        return Result.success(data);
    }
}
