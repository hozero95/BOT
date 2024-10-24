package com.example.bot.test;

import com.example.bot.core.config.ResponseResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("SpellCheckingInspection")
@RestController
@RequestMapping("/api/test-auth")
public class TestAuthController {
    @GetMapping("/")
    public ResponseResult<?> test() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userName = authentication.getName(); // 현재 세션의 사용자 아이디 => 이 프로젝트에서는 usercd
        String role = authentication.getAuthorities().iterator().next().getAuthority(); // 현재 세션의 사용자 권한

        return ResponseResult.ofSuccess("success", "userName: " + userName + ", role: " + role);
    }
}
