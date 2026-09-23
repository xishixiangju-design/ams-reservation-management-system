package com.trae.ams.controller;

import com.trae.ams.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Result<String> healthCheck() {
        return Result.success("AMS Backend is running. Status: OK");
    }
}
