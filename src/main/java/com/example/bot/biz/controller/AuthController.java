package com.example.bot.biz.controller;

import com.example.bot.biz.dto.auth.LoginDTO;
import com.example.bot.biz.service.AuthService;
import com.example.bot.core.config.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("SpellCheckingInspection")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 로그인
     *
     * @param response p1
     * @param loginDTO p2
     * @return ResponseResult<?>
     */
    @PostMapping("/login")
    public ResponseResult<?> login(HttpServletResponse response, @Valid @RequestBody LoginDTO.Request loginDTO) {
        return authService.login(response, loginDTO);
    }

    /**
     * 로그아웃
     *
     * @param request  p1
     * @param response p2
     * @param usercd   p3
     * @return ResponseResult<?>
     */
    @PostMapping("/logout/{usercd}")
    public ResponseResult<?> logout(HttpServletRequest request, HttpServletResponse response, @PathVariable("usercd") String usercd) {
        return authService.logout(request, response, usercd);
    }

    /**
     * Access Token 재발급
     *
     * @param request  p1
     * @param response p2
     * @return ResponseResult<?>
     */
    @Operation(summary = "Access Token 재발급")
    @PostMapping("/refresh")
    public ResponseResult<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        return authService.refresh(request, response);
    }
}
