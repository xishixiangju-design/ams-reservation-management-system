package com.trae.ams.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置类
 * 
 * 功能说明：
 * 从配置文件中以 "alipay" 为前缀读取支付宝支付相关的配置参数，
 * 用于初始化支付宝SDK客户端，完成支付、退款等操作。
 * 
 * 配置示例（application.yml）：
 * alipay:
 *   app-id: 1234567890
 *   private-key: xxx
 *   public-key: yyy
 *   return-url: https://xxx/callback
 *   notify-url: https://xxx/notify
 */
@Component
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayConfig {
    /** 支付宝应用ID，在支付宝开放平台获取 */
    private String appId;
    
    /** 应用私钥，用于对请求进行签名 */
    private String privateKey;
    
    /** 支付宝公钥，用于验证支付宝返回的签名 */
    private String publicKey;
    
    /** 支付宝网关地址，默认为沙箱环境地址 */
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    
    /** 支付完成后前端跳转地址 */
    private String returnUrl;
    
    /** 支付结果异步通知地址（服务端回调） */
    private String notifyUrl;
    
    /** 签名算法类型，默认为RSA2（推荐） */
    private String signType = "RSA2";
    
    /** 请求格式，默认为JSON */
    private String format = "json";
    
    /** 请求编码，默认为UTF-8 */
    private String charset = "UTF-8";
}
