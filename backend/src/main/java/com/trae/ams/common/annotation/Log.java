package com.trae.ams.common.annotation;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 * 
 * 使用方式：在Controller方法上添加 @Log 注解，即可自动记录该操作的日志信息
 * 配合 OperationLogAspect 切面类使用，实现操作日志的自动采集和存储
 * 
 * 示例：@Log(title = "预约管理", businessType = "INSERT")
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /** 日志所属模块名称，例如"预约管理"、"技师管理"等 */
    String title() default "";

    /** 业务操作类型，例如INSERT、UPDATE、DELETE、OTHER等 */
    String businessType() default "OTHER";

    /** 是否保存请求的参数，默认保存，便于排查问题 */
    boolean isSaveRequestData() default true;

    /** 是否保存响应的参数，默认保存，便于排查问题 */
    boolean isSaveResponseData() default true;
}
