package com.trae.ams.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.config.AlipayConfig;
import com.trae.ams.entity.AmsAppointment;
import com.trae.ams.entity.AmsTransaction;
import com.trae.ams.mapper.AmsAppointmentMapper;
import com.trae.ams.mapper.AmsTransactionMapper;
import com.trae.ams.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private AmsAppointmentMapper appointmentMapper;

    @Autowired
    private AmsTransactionMapper transactionMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Mock Merchant Balance Service
    private void updateMerchantBalance(Long storeId, BigDecimal amount) {
        log.info("Updating merchant balance for store {}: subtract {}", storeId, amount);
        // Implementation omitted
    }
    
    // Mock Accounting Service
    private void createAccountingEntry(Long appointmentId, BigDecimal amount, String type) {
        log.info("Creating accounting entry for appt {}: {} {}", appointmentId, type, amount);
        // Implementation omitted: 借：预收账款 贷：银行存款
    }

    @Override
    @Transactional
    public String initiateAlipay(Long appointmentId) {
        // 1. Check Appointment
        AmsAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("Appointment not found");
        }
        if ("PAID".equals(appointment.getPaymentStatus())) {
            throw new BusinessException("Appointment already paid");
        }
        if (appointment.getStatus() == 3) { // 3: Cancelled
             throw new BusinessException("Cannot pay for cancelled appointment");
        }

        // 2. Create Transaction Record
        String outTradeNo = UUID.randomUUID().toString().replace("-", "");
        
        AmsTransaction transaction = new AmsTransaction();
        transaction.setStoreId(appointment.getStoreId());
        transaction.setAppointmentId(appointmentId);
        transaction.setOutTradeNo(outTradeNo);
        transaction.setAmount(appointment.getTotalAmount());
        transaction.setPaymentMethod("ALIPAY");
        transaction.setStatus("PENDING");
        transaction.setType("PAYMENT");
        transactionMapper.insert(transaction);

        // 3. Call Alipay SDK
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    alipayConfig.getCharset(),
                    alipayConfig.getPublicKey(),
                    alipayConfig.getSignType()
            );

            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setReturnUrl(alipayConfig.getReturnUrl());
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);
            bizContent.put("total_amount", appointment.getTotalAmount().toString());
            bizContent.put("subject", "Appointment Payment: " + appointmentId);
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            
            request.setBizContent(objectMapper.writeValueAsString(bizContent));
            
            return alipayClient.pageExecute(request).getBody();
            
        } catch (AlipayApiException | com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Alipay init failed", e);
            throw new BusinessException("Payment initiation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String handleAlipayNotify(Map<String, String> params) {
        log.info("Receive Alipay notify: {}", params);
        try {
            // 1. Verify Signature
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (!signVerified) {
                log.error("Alipay signature verification failed! Params: {}", params);
                return "fail";
            }
            log.info("Alipay signature verified successfully.");

            // 2. Check Trade Status
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.warn("Alipay notify status not success: {}", tradeStatus);
                return "success"; // Not a success status, but we received it.
            }

            // 3. Update Transaction & Appointment
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String gmtPayment = params.get("gmt_payment");
            String totalAmount = params.get("total_amount");
            String appId = params.get("app_id");

            // Verify App ID
            if (!alipayConfig.getAppId().equals(appId)) {
                log.error("Alipay notify AppID mismatch! Config: {}, Received: {}", alipayConfig.getAppId(), appId);
                return "fail";
            }

            AmsTransaction transaction = transactionMapper.selectByOutTradeNo(outTradeNo);
            if (transaction == null) {
                log.error("Transaction not found: {}", outTradeNo);
                return "fail";
            }

            // Verify Total Amount
            if (transaction.getAmount().compareTo(new BigDecimal(totalAmount)) != 0) {
                log.error("Alipay notify amount mismatch! DB: {}, Received: {}", transaction.getAmount(), totalAmount);
                return "fail";
            }
            
            if ("SUCCESS".equals(transaction.getStatus())) {
                log.info("Transaction already processed: {}", outTradeNo);
                return "success"; // Already processed
            }
            
            // Update Transaction
            LocalDateTime payTime = LocalDateTime.now();
            if (gmtPayment != null) {
                payTime = LocalDateTime.parse(gmtPayment, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            
            transactionMapper.updateStatus(outTradeNo, "SUCCESS", tradeNo, "ALIPAY", payTime);
            log.info("Transaction status updated to SUCCESS: {}", outTradeNo);
            
            // Update Appointment
            AmsAppointment appointment = appointmentMapper.selectById(transaction.getAppointmentId());
            if (appointment != null) {
                appointment.setPaymentStatus("PAID");
                appointment.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION); // 5: Pending Consumption
                appointmentMapper.update(appointment);
                log.info("Appointment status updated to PAID and PENDING_CONSUMPTION: {}", appointment.getId());
            }
            
            return "success";
            
        } catch (Exception e) {
            log.error("Handle Alipay notify failed", e);
            return "fail";
        }
    }
    
    @Override
    public AmsTransaction getTransactionByOutTradeNo(String outTradeNo) {
        return transactionMapper.selectByOutTradeNo(outTradeNo);
    }

    @Override
    @Transactional
    public AmsTransaction syncStatusWithAlipay(String outTradeNo) {
        // 1. Get local transaction
        AmsTransaction transaction = transactionMapper.selectByOutTradeNo(outTradeNo);
        if (transaction == null) {
            log.warn("Sync status failed: Transaction not found for {}", outTradeNo);
            return null;
        }

        // If already success, return immediately
        if ("SUCCESS".equals(transaction.getStatus())) {
            return transaction;
        }

        // 2. Query Alipay
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    alipayConfig.getCharset(),
                    alipayConfig.getPublicKey(),
                    alipayConfig.getSignType()
            );

            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);
            request.setBizContent(objectMapper.writeValueAsString(bizContent));

            AlipayTradeQueryResponse response = alipayClient.execute(request);
            
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                log.info("Alipay query response for {}: status={}, amount={}", outTradeNo, tradeStatus, response.getTotalAmount());
                
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // Update Transaction
                    String tradeNo = response.getTradeNo();
                    LocalDateTime payTime = LocalDateTime.now(); // Default to now if send_pay_date missing
                    if (response.getSendPayDate() != null) {
                         // Alipay date format varies, handle carefully or use now
                         // For simplicity, using current time or parsing if standard
                    }

                    // Double check amount
                    BigDecimal payAmount = new BigDecimal(response.getTotalAmount());
                    if (transaction.getAmount().compareTo(payAmount) == 0) {
                        transactionMapper.updateStatus(outTradeNo, "SUCCESS", tradeNo, "ALIPAY", payTime);
                        transaction.setStatus("SUCCESS");
                        
                        // Update Appointment
                        AmsAppointment appointment = appointmentMapper.selectById(transaction.getAppointmentId());
                        if (appointment != null) {
                            appointment.setPaymentStatus("PAID");
                            appointment.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION);
                            appointmentMapper.update(appointment);
                        }
                    } else {
                        log.error("Alipay query amount mismatch! DB: {}, Received: {}", transaction.getAmount(), payAmount);
                    }
                }
            } else {
                log.warn("Alipay query failed for {}: {} - {}", outTradeNo, response.getCode(), response.getSubMsg());
            }
            
        } catch (Exception e) {
            log.error("Sync status with Alipay failed", e);
        }

        return transaction;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void refund(Long appointmentId, BigDecimal amount, String reason, String operator) {
        // 1. Get original successful transaction
        AmsTransaction originalTx = transactionMapper.selectSuccessPaymentByAppointmentId(appointmentId);
        if (originalTx == null) {
            throw new BusinessException("No successful payment found for this appointment");
        }
        
        // Check if amount > 0
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Refund amount is 0 or negative, skipping payment gateway refund.");
            return;
        }

        AmsAppointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null) throw new BusinessException("Appointment not found");

        String refundTradeNo = "RF" + appointmentId + String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        
        // 2. Create Refund Transaction Record
        AmsTransaction refundTx = new AmsTransaction();
        refundTx.setStoreId(appt.getStoreId());
        refundTx.setAppointmentId(appointmentId);
        refundTx.setOutTradeNo(refundTradeNo);
        refundTx.setAmount(amount); 
        refundTx.setPaymentMethod("ALIPAY"); 
        refundTx.setStatus("PENDING");
        refundTx.setType("REFUND");
        refundTx.setReason(reason);
        refundTx.setOperator(operator);
        refundTx.setCreateTime(LocalDateTime.now());
        refundTx.setRelatedTransactionId(originalTx.getId()); // Link to original payment
        
        transactionMapper.insert(refundTx);
        
        // 3. Call Alipay Refund API
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    alipayConfig.getCharset(),
                    alipayConfig.getPublicKey(),
                    alipayConfig.getSignType()
            );

            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", originalTx.getOutTradeNo()); // Use original out_trade_no
            bizContent.put("refund_amount", amount.toString());
            bizContent.put("out_request_no", refundTradeNo); // Unique ID for this refund request
            bizContent.put("refund_reason", reason);

            request.setBizContent(objectMapper.writeValueAsString(bizContent));
            
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            
            if (response.isSuccess()) {
                log.info("Alipay refund success: {}", response.getBody());
                
                refundTx.setStatus("SUCCESS");
                refundTx.setPayTime(LocalDateTime.now());
                refundTx.setTradeNo(response.getTradeNo()); // Alipay's trade no
                
                // Update with specific fields if available in mapper, or use updateStatus which sets minimal fields
                // Here we manually update fields
                transactionMapper.updateStatus(refundTradeNo, "SUCCESS", response.getTradeNo(), "ALIPAY", LocalDateTime.now());
                
                // 4. Update Merchant Balance
                updateMerchantBalance(appt.getStoreId(), amount);
                
                // 5. Accounting Entry
                createAccountingEntry(appointmentId, amount, "REFUND");
                
            } else {
                log.error("Alipay refund failed: {} - {}", response.getCode(), response.getSubMsg());
                refundTx.setStatus("FAILED");
                transactionMapper.updateStatus(refundTradeNo, "FAILED", null, "ALIPAY", null);
                throw new BusinessException("Refund failed: " + response.getSubMsg());
            }
            
        } catch (Exception e) {
            log.error("Refund exception", e);
            refundTx.setStatus("FAILED");
            transactionMapper.updateStatus(refundTradeNo, "FAILED", null, "ALIPAY", null);
            throw new BusinessException("Refund processing failed: " + e.getMessage());
        }
    }
}
