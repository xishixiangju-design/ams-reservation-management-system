package com.trae.ams.common.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * 密码加密工具类
 * 规范：MD5 + 16位随机盐 + 1000次迭代
 */
public class PasswordUtil {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 1000;

    /**
     * 生成随机盐
     */
    public static String generateSalt() {
        return RandomUtil.randomString(SALT_LENGTH);
    }

    /**
     * 密码加密
     * @param password 明文密码
     * @param salt 盐
     * @return 加密后的密文
     */
    public static String encrypt(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        
        // Hutool Digester 支持加盐和迭代次数
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        md5.setSalt(salt.getBytes());
        md5.setDigestCount(ITERATIONS);
        
        return md5.digestHex(password);
    }
    
    /**
     * 校验密码
     */
    public static boolean verify(String inputPassword, String salt, String dbPassword) {
        String encrypted = encrypt(inputPassword, salt);
        return encrypted.equals(dbPassword);
    }
}
