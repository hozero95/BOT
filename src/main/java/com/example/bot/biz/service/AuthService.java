package com.example.bot.biz.service;

import com.example.bot.biz.dto.auth.LoginDTO;
import com.example.bot.biz.entity.Auth;
import com.example.bot.biz.entity.User;
import com.example.bot.biz.repository.AuthRepository;
import com.example.bot.biz.repository.RefreshRepository;
import com.example.bot.biz.repository.UserRepository;
import com.example.bot.core.config.ResponseResult;
import com.example.bot.core.security.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@SuppressWarnings("SpellCheckingInspection")
@Slf4j
@Service
public class AuthService {
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(JwtUtil jwtUtil, RefreshRepository refreshRepository, UserRepository userRepository, AuthRepository authRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * 로그인
     *
     * @param response p1
     * @param loginDTO p2
     * @return ResponseResult<?>
     */
    public ResponseResult<?> login(HttpServletResponse response, LoginDTO.Request loginDTO) {
        String usercd = loginDTO.getUsercd();
        String password = loginDTO.getPassword();

        log.debug("로그인 시도: {} {}", usercd, password);

        User user = userRepository.findByUsercd(usercd);
        if (user == null) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, "user does not exist");
        }
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, "password does not match");
        }

        Auth auth = authRepository.findByUsercd(usercd);
        if (auth == null) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, "do not have permission");
        }

        String role = auth.getAuth();

        String accessToken = jwtUtil.createJwt("Access-Token", usercd, role, JwtUtil.ACCESS_TOKEN_EXPIRE);
        String refreshToken = jwtUtil.createJwt("Refresh-Token", usercd, role, JwtUtil.REFRESH_TOKEN_EXPIRE);

        refreshRepository.deleteByUsercd(usercd);
        jwtUtil.addRefreshToken(usercd, refreshToken, JwtUtil.REFRESH_TOKEN_EXPIRE);

        response.setHeader("Access-Token", accessToken);
        response.addCookie(jwtUtil.createRefreshCookie(refreshToken));

        LoginDTO.Response loginResponse = new LoginDTO.Response();
        loginResponse.setUsercd(usercd);
        loginResponse.setUsernm(user.getUsernm());

        return ResponseResult.ofSuccess("success", loginResponse);
    }

    /**
     * 로그아웃
     *
     * @param request  p1
     * @param response p2
     * @param usercd   p3
     * @return ResponseResult<?>
     */
    public ResponseResult<?> logout(HttpServletRequest request, HttpServletResponse response, String usercd) {
        // 파라미터 검증
        if (usercd.isEmpty()) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, "user does not exist");
        }

        // 로그인 한 상태인지 확인
        Boolean isLogin = refreshRepository.existsByUsercd(usercd);
        if (!isLogin) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, "login info is not exist");
        }

        // get refresh token
        String refreshToken = jwtUtil.getRefreshTokenByCookie(request);

        // Refresh 토큰 검증
        String validRefreshToken = validRefreshToken(refreshToken);
        if (!validRefreshToken.equals("success")) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, validRefreshToken);
        }

        // 로그아웃 진행
        // Refresh 토큰 DB 제거
        refreshRepository.deleteByToken(refreshToken);

        // Refresh 토큰 Cookie 제거
        Cookie cookie = new Cookie("Refresh-Token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.setHeader("Access-Token", null);
        response.addCookie(cookie);

        return ResponseResult.ofSuccess("success", null);
    }

    /**
     * Access Token 재발급
     *
     * @param request  p1
     * @param response p2
     * @return ResponseResult<?>
     */
    public ResponseResult<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        // get refresh token
        String refreshToken = jwtUtil.getRefreshTokenByCookie(request);

        // Refresh 토큰 검증
        String validRefreshToken = validRefreshToken(refreshToken);
        if (!validRefreshToken.equals("success")) {
            return ResponseResult.ofFailure(HttpStatus.BAD_REQUEST, validRefreshToken);
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // Access Token 생성
        String newAccess = jwtUtil.createJwt("Access-Token", username, role, JwtUtil.ACCESS_TOKEN_EXPIRE);

        // Response
        response.setHeader("Access-Token", newAccess);

        // Access Token 갱신할 때 Refresh Token도 갱신하고 싶으면 주석 해제하면 됨
//        // Refresh Token 생성
//        String newRefresh = jwtUtil.createJwt("Access-Token", username, role, JwtUtil.REFRESH_TOKEN_EXPIRE);
//        // 기존 Refresh Token 삭제 및 신규 Refresh Token 저장
//        refreshRepository.deleteByToken(refreshToken);
//        jwtUtil.addRefreshToken(username, newRefresh, JwtUtil.REFRESH_TOKEN_EXPIRE);
//        response.addCookie(jwtUtil.createRefreshCookie(newRefresh));

        return ResponseResult.ofSuccess("success", null);
    }

    /**
     * Refresh 토큰 검증 메소드
     *
     * @param refreshToken p1
     * @return String
     */
    public String validRefreshToken(String refreshToken) {
        // 토큰 유무 검증
        if (refreshToken == null || refreshToken.isEmpty()) {
            return "refresh token is empty";
        }

        // 토큰 만료 검증
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            return "refresh token is expired";
        }

        // Refresh Token 맞는지 확인
        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("Refresh-Token")) {
            return "refresh token is invalid";
        }

        // DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByToken(refreshToken);
        if (!isExist) {
            return "refresh token is not exist";
        }

        return "success";
    }
}