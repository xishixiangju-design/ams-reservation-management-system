package com.trae.ams.common.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.trae.ams.common.annotation.Log;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.entity.SysOperationLog;
import com.trae.ams.service.AsyncLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面类
 * 
 * 功能说明：
 * 通过AOP切面拦截所有带有 @Log 注解的Controller方法，
 * 自动采集操作信息（模块、业务类型、请求参数、响应结果、执行耗时等），
 * 并通过异步日志服务将操作记录持久化到数据库。
 * 
 * 处理流程：
 * 1. 环绕通知拦截带有 @Log 注解的方法
 * 2. 记录方法开始执行时间
 * 3. 执行目标方法，根据成功/失败设置状态
 * 4. 采集用户信息、请求信息、方法信息等
 * 5. 调用异步日志服务保存日志记录
 */
@Aspect
@Component
public class OperationLogAspect {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    /** 异步日志服务，用于异步保存操作日志到数据库 */
    @Resource
    private AsyncLogService asyncLogService;

    /**
     * 环绕通知：拦截所有带有 @Log 注解的Controller方法
     * 
     * @param point 切点，包含目标方法的信息
     * @param controllerLog @Log 注解实例，包含日志配置信息
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@annotation(controllerLog)")
    public Object doAround(ProceedingJoinPoint point, Log controllerLog) throws Throwable {
        // 记录方法开始执行的时间戳，用于计算耗时
        long startTime = System.currentTimeMillis();
        // 创建操作日志实体对象
        SysOperationLog operLog = new SysOperationLog();
        Object result = null;
        try {
            // 执行目标方法
            result = point.proceed();
            operLog.setStatus(0); // 状态0表示操作成功
            // 如果配置了保存响应数据，则截取前2000个字符保存
            if (controllerLog.isSaveResponseData()) {
                operLog.setJsonResult(StrUtil.sub(JSONUtil.toJsonStr(result), 0, 2000));
            }
        } catch (Exception e) {
            operLog.setStatus(1); // 状态1表示操作失败
            // 截取异常信息前1000个字符保存
            operLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 1000));
            throw e; // 重新抛出异常，不影响正常异常处理流程
        } finally {
            // 计算方法执行耗时（毫秒）
            long costTime = System.currentTimeMillis() - startTime;
            operLog.setCostTime(costTime);
            // 保存日志记录
            saveLog(point, controllerLog, operLog);
        }
        return result;
    }

    /**
     * 保存操作日志到数据库
     * 
     * 处理流程：
     * 1. 设置基础信息（模块名称、业务类型）
     * 2. 获取当前用户信息（用户ID、门店ID）
     * 3. 获取HTTP请求信息（IP地址、请求方法、请求参数）
     * 4. 调用异步日志服务持久化日志记录
     * 
     * @param point 切点，包含目标方法信息
     * @param controllerLog @Log 注解实例
     * @param operLog 操作日志实体
     */
    private void saveLog(ProceedingJoinPoint point, Log controllerLog, SysOperationLog operLog) {
        try {
            // 1. 设置基础信息
            operLog.setModule(controllerLog.title());
            operLog.setBusinessType(controllerLog.businessType());
            
            // 2. 获取用户信息
            Long userId = null;
            try {
                userId = UserContext.getUserId();
            } catch (Exception e) {
                // 如果没有用户上下文则忽略（如公开接口）
            }
            operLog.setUserId(userId);
            operLog.setUsername("User-" + (userId != null ? userId : "Unknown")); 
            try {
                operLog.setStoreId(UserContext.getStoreId());
            } catch (Exception e) {
                // 如果没有门店信息则忽略
            }

            // 3. 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setIpAddr(request.getRemoteAddr());  // 客户端IP地址
                operLog.setRequestMethod(request.getMethod()); // HTTP请求方法（GET/POST等）
                operLog.setMethod(point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName()); // 完整方法名
                
                // 如果配置了保存请求数据，则序列化第一个参数
                if (controllerLog.isSaveRequestData()) {
                    Object[] args = point.getArgs();
                    if (args != null && args.length > 0) {
                         try {
                             // 将请求参数序列化为JSON，截取前2000个字符
                             String params = JSONUtil.toJsonStr(args[0]); 
                             operLog.setOperParam(StrUtil.sub(params, 0, 2000));
                         } catch (Exception e) {
                             // 忽略序列化错误
                         }
                    }
                }
            }
            
            // 4. 通过异步服务将日志入库
            asyncLogService.saveSysLog(operLog);
            
        } catch (Exception e) {
            log.error("保存操作日志异常", e);
        }
    }
}
