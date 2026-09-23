package com.trae.ams.controller;

import com.trae.ams.common.annotation.Log;
import com.trae.ams.common.result.Result;
import com.trae.ams.entity.PaymentMethod;
import com.trae.ams.mapper.PaymentMethodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payment-method")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodMapper paymentMethodMapper;

    /**
     * 获取所有支付方式（管理端）
     */
    @GetMapping("/list")
    public Result<List<PaymentMethod>> list() {
        return Result.success(paymentMethodMapper.selectAll());
    }

    /**
     * 获取可用支付方式（客户端下拉框用）
     */
    @GetMapping("/active")
    public Result<List<PaymentMethod>> listActive() {
        return Result.success(paymentMethodMapper.selectActive());
    }

    /**
     * 更新支付配置
     */
    @PostMapping("/update")
    @Log(title = "支付管理", businessType = "UPDATE")
    public Result<Void> update(@RequestBody PaymentMethod paymentMethod) {
        if (paymentMethod.getId() == null) {
            return Result.error("ID cannot be null");
        }
        paymentMethodMapper.update(paymentMethod);
        return Result.success();
    }
    
    /**
     * 新增支付方式
     */
    @PostMapping("/add")
    @Log(title = "支付管理", businessType = "INSERT")
    public Result<Void> add(@RequestBody PaymentMethod paymentMethod) {
        paymentMethodMapper.insert(paymentMethod);
        return Result.success();
    }
}
