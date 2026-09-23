package com.trae.ams.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import com.trae.ams.common.cache.LocalCache;
import com.trae.ams.common.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class CaptchaController {
//@RequestMapping("/api")
    @Resource
    private LocalCache localCache;

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        // Generate line captcha: width 130, height 48, 4 chars, 150 lines interference
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(130, 48, 4, 150);
        
        // Get code and image
        String code = lineCaptcha.getCode();
        String imageBase64 = lineCaptcha.getImageBase64();
        
        // Generate UUID
        String uuid = UUID.randomUUID().toString(true); // simple UUID without hyphens
        
        // Store in LocalCache with 300s expiration
        // Key format: captcha:${uuid}
        // Value: code (ignore case when verifying)
        localCache.set("captcha:" + uuid, code, 300, TimeUnit.SECONDS);
        
        // Return result
        Map<String, String> data = new HashMap<>();
        data.put("uuid", uuid);
        data.put("image", "data:image/png;base64," + imageBase64);
        
        return Result.success(data);
    }
}
