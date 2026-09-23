package com.trae.ams.common.cache;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.convert.Convert;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存实现类，用于替代Redis的单节点部署方案
 * 
 * 功能说明：
 * - 基于Hutool的TimedCache实现定时过期的本地缓存
 * - 默认缓存超时时间为1小时，每5秒执行一次过期清理
 * - 提供与Redis类似的操作接口（set/get/delete/increment/expire）
 * - 适用于单节点部署场景，多节点部署需替换为Redis实现
 * 
 * 典型使用场景：
 * - 验证码缓存
 * - IP限流计数
 * - 登录失败次数限制
 */
@Component
public class LocalCache {

    /** Hutool定时缓存实例，键和值均为String类型 */
    private final TimedCache<String, String> cache;

    /**
     * 构造方法：初始化本地缓存
     * - 默认超时时间：1小时（3600秒）
     * - 过期清理间隔：5秒
     */
    public LocalCache() {
        // 创建1小时超时的定时缓存
        this.cache = CacheUtil.newTimedCache(3600 * 1000);
        // 每5秒执行一次过期键清理任务
        this.cache.schedulePrune(5000);
    }

    /**
     * 设置缓存值（使用默认超时时间1小时）
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, String value) {
        cache.put(key, value);
    }

    /**
     * 设置缓存值（指定超时时间）
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        cache.put(key, value, unit.toMillis(timeout));
    }

    /**
     * 获取缓存值
     * 
     * @param key 缓存键
     * @return 缓存值，键不存在或已过期则返回null
     */
    public String get(String key) {
        return cache.get(key);
    }

    /**
     * 删除缓存键
     * 
     * @param key 要删除的缓存键
     */
    public void delete(String key) {
        cache.remove(key);
    }

    /**
     * 判断缓存键是否存在
     * 
     * @param key 缓存键
     * @return 键存在且未过期返回true，否则返回false
     */
    public boolean hasKey(String key) {
        return cache.containsKey(key);
    }

    /**
     * 自增操作（线程安全）
     * 对指定键的值执行加1操作，如果键不存在则从0开始计数
     * 
     * 注意：此操作使用synchronized保证线程安全
     * 调用方（如AuthServiceImpl）通常在increment后立即调用expire设置过期时间
     * 
     * @param key 缓存键
     * @return 自增后的值
     */
    public Long increment(String key) {
        synchronized (this) {
            String val = cache.get(key);
            long count = 0;
            if (val != null) {
                // 将字符串值转换为Long类型，转换失败则默认为0
                count = Convert.toLong(val, 0L);
            }
            count++;
            // 将自增后的值写回缓存
            cache.put(key, String.valueOf(count)); 
            return count;
        }
    }

    /**
     * 设置缓存键的过期时间
     * 
     * @param key 缓存键
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        String val = cache.get(key);
        if (val != null) {
            // 重新放入缓存并设置新的过期时间
            cache.put(key, val, unit.toMillis(timeout));
        }
    }
}
