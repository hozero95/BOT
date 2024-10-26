package com.example.bot.core.security.filter;

import com.example.bot.biz.entity.User;
import com.example.bot.biz.repository.AuthRepository;
import com.example.bot.biz.repository.RefreshRepository;
import com.example.bot.core.config.RequestMatcherHolder;
import com.example.bot.core.config.ResponseResult;
import com.example.bot.core.security.util.CustomUserDetails;
import com.example.bot.core.security.util.JwtUtil;
import com.example.bot.core.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * JWT Filter
 */
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthRepository authRepository;
    private final RefreshRepository refreshRepository;

    public JwtFilter(JwtUtil jwtUtil, AuthRepository authRepository, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.authRepository = authRepository;
        this.refreshRepository = refreshRepository;
    }

    /**
     * JwtFilter 에서 실행되는 메소드
     *
     * @param request     p1
     * @param response    p2
     * @param filterChain p3
     * @throws ServletException e1
     * @throws IOException      e2
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // header 에서 access 토큰 조회
        String accessToken = request.getHeader("Access-Token");

        // 토큰 유무 검증
        if (accessToken == null || accessToken.isEmpty()) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "access token is empty")).getBytes());
            response.setStatus(401);
            return;
        }

        // 토큰 만료 검증 => 다음 필터 진행하지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "access token is expired")).getBytes());
            response.setStatus(401);
            return;
        }

        // Token Category 맞는지 확인
        String accessCategory = jwtUtil.getCategory(accessToken);
        if (!accessCategory.equals("Access-Token")) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "access token is invalid")).getBytes());
            response.setStatus(401);
            return;
        }

        // Access 토큰이 만료 직전일 때 갱신
        if (isAccessTokenExpiringSoon(accessToken)) {
            // Cookie 에서 refresh 토큰 조회
            String refreshToken = null;
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Refresh-Token")) {
                    refreshToken = cookie.getValue();
                }
            }

            // 토큰 유무 검증
            if (refreshToken == null || refreshToken.isEmpty()) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "refresh token is empty")).getBytes());
                response.setStatus(401);
                return;
            }

            // 토큰 만료 검증 => 다음 필터 진행하지 않음
            try {
                jwtUtil.isExpired(refreshToken);
            } catch (ExpiredJwtException e) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "refresh token is expired")).getBytes());
                response.setStatus(401);
                return;
            }

            // Token Category 맞는지 확인
            String refreshCategory = jwtUtil.getCategory(refreshToken);
            if (!refreshCategory.equals("Access-Token")) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "refresh token is invalid")).getBytes());
                response.setStatus(401);
                return;
            }

            // Refresh 토큰이 DB에 저장되어 있는지 확인
            Boolean isExist = refreshRepository.existsByToken(refreshToken);
            if (!isExist) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "refresh token is not exist")).getBytes());
                response.setStatus(401);
                return;
            }

            String username = jwtUtil.getUsername(refreshToken);
            String role = jwtUtil.getRole(refreshToken);
            String newAccess = jwtUtil.createJwt("Access-Token", username, role, JwtUtil.ACCESS_TOKEN_EXPIRE);
            response.setHeader("Access-Token", newAccess);
        }

        // 토큰에서 username 획득
        String username = jwtUtil.getUsername(accessToken);

        // User 객체 생성
        User user = new User();
        user.setUsercd(username);
        user.setPassword("temporary"); // 임시 암호 발급

        // UserDetails 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(user, authRepository);
        // Spring Security 인증 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /**
     * 특정 URL 들을 JwtFiler 를 거치지 않게 하는 메소드
     *
     * @param request p1
     * @return boolean
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        RequestMatcherHolder requestMatcherHolder = new RequestMatcherHolder();
        String[] excludePath = StringUtils.stringListRemoveSuffix(requestMatcherHolder.getPERMIT_ALL_URLS(), "**").toArray(new String[0]);
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }

    /**
     * Token 만료시간이 5분 이하로 남았는지 확인하는 메소드
     *
     * @param token p1
     * @return boolean
     */
    private boolean isAccessTokenExpiringSoon(String token) {
        Date expirationDate = jwtUtil.isExpired(token);
        return expirationDate.getTime() - System.currentTimeMillis() < 5 * 60 * 1000;
    }
}
