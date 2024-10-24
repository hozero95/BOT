package com.example.bot.core.security.filter;

import com.example.bot.biz.entity.User;
import com.example.bot.biz.repository.AuthRepository;
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

/**
 * JWT Filter
 */
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthRepository authRepository;

    public JwtFilter(JwtUtil jwtUtil, AuthRepository authRepository) {
        this.jwtUtil = jwtUtil;
        this.authRepository = authRepository;
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
        if (accessToken == null) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "access token is empty")).getBytes());
            response.setStatus(401);
//            filterChain.doFilter(request, response); // 다음 필터 진행
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

        // Access Token 맞는지 확인
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("Access-Token")) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "access token is invalid")).getBytes());
            response.setStatus(401);
            return;
        }

        // 토큰에서 username, role 획득
        String username = jwtUtil.getUsername(accessToken);
//        String role = jwtUtil.getRole(accessToken);

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
}
