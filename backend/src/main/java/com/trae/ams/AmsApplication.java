package com.trae.ams;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AMS（预约管理系统）应用程序启动类
 * 该类是Spring Boot应用的入口点，负责初始化应用上下文
 * 
 * 功能说明：
 * - @SpringBootApplication: Spring Boot自动配置注解，启用自动配置和组件扫描
 * - @EnableAsync: 启用异步方法执行支持，用于异步日志记录等场景
 * - @MapperScan: 扫描MyBatis Mapper接口所在包，自动注册Mapper代理
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.trae.ams.mapper")
public class AmsApplication {

    /**
     * 应用程序主入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 启动Spring Boot应用，加载配置并初始化所有Bean
            SpringApplication.run(AmsApplication.class, args);
        } catch (Exception e) {
            // 捕获启动异常并打印堆栈信息，便于排查问题
            e.printStackTrace();
        }
    }

}
