package com.example.bot.biz.controller;

import com.example.bot.biz.service.TokenService;
import com.example.bot.core.config.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
@Tag(name = "Token API", description = "Token 관련 API")
public class TokenController {
    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Access Token 재발급
     *
     * @param request  p1
     * @param response p2
     * @return ResponseResult<?>
     */
    @Operation(summary = "Access Token 재발급")
    @PostMapping("/reissue")
    public ResponseResult<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return tokenService.reissue(request, response);
    }
}
