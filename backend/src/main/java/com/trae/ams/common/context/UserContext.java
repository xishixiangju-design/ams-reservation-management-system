package com.trae.ams.common.context;

/**
 * 用户上下文工具类
 * 
 * 功能说明：
 * 基于ThreadLocal存储当前请求的登录用户信息，
 * 在请求线程内共享用户ID、门店ID、管理员标识等信息。
 * 
 * 使用流程：
 * 1. AuthInterceptor拦截请求后，从JWT令牌中解析用户信息
 * 2. 调用set方法将用户信息存入ThreadLocal
 * 3. 业务代码通过get方法获取当前用户信息
 * 4. 请求结束后调用clear方法清理ThreadLocal，防止内存泄漏
 * 
 * 注意：必须在请求结束后调用clear()，否则会导致ThreadLocal内存泄漏
 */
public class UserContext {
    /** 当前登录用户ID */
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    /** 当前用户所属门店ID */
    private static final ThreadLocal<Long> STORE_ID = new ThreadLocal<>();
    /** 是否为管理员标识 */
    private static final ThreadLocal<Boolean> IS_ADMIN = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     * @return 用户ID，未设置时返回null
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置当前门店ID
     * @param storeId 门店ID
     */
    public static void setStoreId(Long storeId) {
        STORE_ID.set(storeId);
    }

    /**
     * 获取当前门店ID
     * @return 门店ID，未设置时返回null
     */
    public static Long getStoreId() {
        return STORE_ID.get();
    }

    /**
     * 设置是否为管理员
     * @param isAdmin true表示管理员，false表示普通用户
     */
    public static void setIsAdmin(Boolean isAdmin) {
        IS_ADMIN.set(isAdmin);
    }

    /**
     * 判断当前用户是否为管理员
     * @return 是管理员返回true，否则返回false
     */
    public static boolean isAdmin() {
        return Boolean.TRUE.equals(IS_ADMIN.get());
    }
    
    /**
     * 获取当前StoreId，如果为空则返回默认值1L (兼容旧逻辑)
     */
    public static Long getStoreIdOrDefault() {
        Long storeId = STORE_ID.get();
        return storeId != null ? storeId : 1L;
    }

    /** 是否为超级管理员标识 */
    private static final ThreadLocal<Boolean> IS_SUPER_ADMIN = new ThreadLocal<>();

    /**
     * 设置是否为超级管理员
     * @param isSuperAdmin true表示超级管理员
     */
    public static void setIsSuperAdmin(Boolean isSuperAdmin) {
        IS_SUPER_ADMIN.set(isSuperAdmin);
    }

    /**
     * 判断当前用户是否为超级管理员
     * @return 是超级管理员返回true，否则返回false
     */
    public static boolean isSuperAdmin() {
        return Boolean.TRUE.equals(IS_SUPER_ADMIN.get());
    }
    
    /**
     * 清理所有ThreadLocal变量，防止内存泄漏
     * 必须在请求结束后调用（通常在拦截器的afterCompletion中调用）
     */
    public static void clear() {
        USER_ID.remove();
        STORE_ID.remove();
        IS_ADMIN.remove();
        IS_SUPER_ADMIN.remove();
    }
}
