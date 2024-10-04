package com.example.bot.controller;

import com.example.bot.dto.JoinDTO;
import com.example.bot.dto.ResponseResult;
import com.example.bot.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
     * 회원 가입
     * @param joinDTO
     * @return ResponseResult<Object>
     */
    @PostMapping("/signup")
    public ResponseResult<Object> signup(@Valid @RequestBody JoinDTO joinDTO) {
        authService.signup(joinDTO);

        return ResponseResult.ofSuccess("회원 가입 성공", null);
    }
}
