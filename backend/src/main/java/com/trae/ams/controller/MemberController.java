package com.trae.ams.controller;

import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.result.Result;
import com.trae.ams.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/info")
    public Result<Map<String, Object>> getMemberInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("未登录");
        return Result.success(memberService.getMemberInfo(userId));
    }

    @GetMapping("/rules")
    public Result<Object> getRules() {
        return Result.success(memberService.getLevelRules());
    }
}
