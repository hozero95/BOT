package com.example.bot.controller;

import com.example.bot.dto.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/")
    public ResponseResult<Object> test() {
        return ResponseResult.ofSuccess("테스트 성공", null);
    }
}
