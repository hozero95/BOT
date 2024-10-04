package com.example.bot.test;

import com.example.bot.core.config.ResponseResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-auth")
public class TestAuthController {
    @GetMapping("/")
    public ResponseResult<Object> test() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userName = authentication.getName(); // 현재 세션의 사용자 아이디 => 이 프로젝트에서는 이메일
        String role = authentication.getAuthorities().iterator().next().getAuthority(); // 현재 세션의 사용자 권한

        return ResponseResult.ofSuccess("테스트 성공", "userName: " + userName + ", role: " + role);
    }
}
