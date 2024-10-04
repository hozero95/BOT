package com.example.bot.core.security.filter;

import com.example.bot.biz.entity.User;
import com.example.bot.core.config.RequestMatcherHolder;
import com.example.bot.core.config.ResponseResult;
import com.example.bot.core.security.util.CustomUserDetails;
import com.example.bot.core.security.util.JwtUtil;
import com.example.bot.core.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // request 에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.")).getBytes());
            response.setStatus(401);
            filterChain.doFilter(request, response);
            return; // 조건에 해당하면 메소드 종료 (필수)
        }

        // Bearer 부분 제거
        String token = authorization.substring("Bearer ".length());

        // 토큰 만료 시간 검증
        try {
            jwtUtil.isExpired(token, request, response, filterChain);
        } catch (ExpiredJwtException e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.")).getBytes());
            response.setStatus(401);
            filterChain.doFilter(request, response);
            return; // 조건에 해당하면 메소드 종료 (필수)
        }

        // 토큰에서 username, role 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // User 객체 생성
        User user = new User();
        user.setEmail(username);
        user.setPassword("temporary"); // 임시 암호 발급
        user.setRole(role);

        // UserDetails 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        // Spring Security 인증 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /**
     * 특정 URL 들을 OncePerRequestFilter 를 거치지 않게 하는 메소드
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
}
