package com.trae.ams.service;

import com.trae.ams.entity.AmsTransaction;
import java.util.Map;

public interface PaymentService {
    /**
     * 发起支付宝支付
     * @param appointmentId 预约ID
     * @return 支付宝表单HTML
     */
    String initiateAlipay(Long appointmentId);

    /**
     * 处理支付宝异步通知
     * @param params 支付宝回调参数
     * @return 成功返回 "success", 失败返回 "fail"
     */
    String handleAlipayNotify(Map<String, String> params);
    
    /**
     * 获取交易记录
     */
    AmsTransaction getTransactionByOutTradeNo(String outTradeNo);

    /**
     * 主动查询支付宝订单状态并同步更新本地数据库
     * @param outTradeNo 商户订单号
     * @return 最新的交易记录
     */
    AmsTransaction syncStatusWithAlipay(String outTradeNo);

    /**
     * 处理退款
     * @param appointmentId 预约ID
     * @param amount 退款金额
     * @param reason 退款原因
     * @param operator 操作人
     */
    void refund(Long appointmentId, java.math.BigDecimal amount, String reason, String operator);
}
