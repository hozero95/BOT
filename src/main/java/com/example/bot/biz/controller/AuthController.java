package com.example.bot.biz.controller;

import com.example.bot.biz.service.AuthService;
import com.example.bot.core.config.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Access Token 재발급
     *
     * @param request  p1
     * @param response p2
     * @return ResponseResult<?>
     */
    @PostMapping("/reissue")
    public ResponseResult<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return authService.reissue(request, response);
    }
}
