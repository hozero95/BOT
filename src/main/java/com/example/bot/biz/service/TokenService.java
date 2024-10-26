package com.example.bot.biz.service;

import com.example.bot.biz.repository.RefreshRepository;
import com.example.bot.core.config.ResponseResult;
import com.example.bot.core.security.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public TokenService(JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    /**
     * Access Token 재발급
     *
     * @param request  p1
     * @param response p2
     * @return ResponseResult<?>
     */
    public ResponseResult<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        // get refresh token
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("Refresh-Token")) {
                refreshToken = cookie.getValue();
            }
        }

        // 토큰 유무 검증
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, "refresh token is empty");
        }

        // 토큰 만료 검증
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, "refresh token is expired");
        }

        // Refresh Token 맞는지 확인
        String category = jwtUtil.getCategory(refreshToken);

        if (!category.equals("Refresh-Token")) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, "refresh token is invalid");
        }

        // DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByToken(refreshToken);
        if (!isExist) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, "refresh token is invalid");
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 토큰 생성
        String newAccess = jwtUtil.createJwt("Access-Token", username, role, JwtUtil.ACCESS_TOKEN_EXPIRE);
        String newRefresh = jwtUtil.createJwt("Access-Token", username, role, JwtUtil.REFRESH_TOKEN_EXPIRE);

        // 기존 Refresh 토큰 삭제 및 신규 Refresh 토큰 저장
        refreshRepository.deleteByToken(refreshToken);
        jwtUtil.addRefreshToken(username, newRefresh, JwtUtil.REFRESH_TOKEN_EXPIRE);

        // Response
        response.setHeader("Access-Token", newAccess);
        response.addCookie(jwtUtil.createCookie(newRefresh));

        return ResponseResult.ofSuccess("success", null);
    }
}
