package com.example.bot.controller;

import com.example.bot.dto.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/")
    public ResponseResult<Object> admin() {
        return ResponseResult.ofSuccess("어드민 접근 성공", null);
    }
}
