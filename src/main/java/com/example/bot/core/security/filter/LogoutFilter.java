package com.example.bot.core.security.filter;

import com.example.bot.biz.repository.RefreshRepository;
import com.example.bot.core.config.ResponseResult;
import com.example.bot.core.security.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
public class LogoutFilter extends GenericFilterBean {
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LogoutFilter(final JwtUtil jwtUtil, final RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // path and method verify
        String requestURI = request.getRequestURI();
        if (!requestURI.matches("^/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // get refresh token
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("Refresh-Token")) {
                refreshToken = cookie.getValue();
            }
        }

        // token null check
        if (refreshToken == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // token expired check
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Refresh 토큰인지 확인
        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("Refresh-Token")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByToken(refreshToken);
        if (!isExist) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 로그아웃 진행
        // Refresh 토큰 DB 제거
        refreshRepository.deleteByToken(refreshToken);

        // Refresh 토큰 Cookie 제거
        Cookie cookie = new Cookie("Refresh-Token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofSuccess("로그아웃 성공", null)).getBytes());
        response.setHeader("Access-Token", null);
        response.addCookie(cookie);
        response.setStatus(HttpStatus.OK.value());

        log.debug("로그아웃 성공");
    }
}
