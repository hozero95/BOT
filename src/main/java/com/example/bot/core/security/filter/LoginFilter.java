package com.example.bot.core.jwt;

import com.example.bot.core.config.CustomUserDetails;
import com.example.bot.biz.dto.ResponseResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 클라이언트에서 /login 호출 시 실행되는 메소드
     * @param req
     * @param res
     * @return Authentication
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        // 클라이언트 요청에서 username(email), password 추출
        String email = obtainUsername(req);
        String password = obtainPassword(req);

        System.out.println(email + " " + password);

        // Spring Security 에서 username 과 password 를 검증하기 위해서는 token 에 담아야 함
        UsernamePasswordAuthenticationToken authenticationToken  = new UsernamePasswordAuthenticationToken(email, password, null);

        // token 에 담은 검증을 위한 AuthenticationManager 로 전달
        return authenticationManager.authenticate(authenticationToken);
    }

    /**
     * 로그인 성공 시 실행하는 메소드
     * @param req
     * @param res
     * @param chain
     * @param authentication
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication authentication) throws IOException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String username = customUserDetails.getUsername();
        String role = auth.getAuthority();
        String token = jwtUtil.createJwt(username, role, 60 * 60 * 1000L);

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofSuccess("로그인 성공", token)).getBytes());
        res.addHeader("Authorization", "Bearer " + token);
    }

    /**
     * 로그인 실패 시 실행하는 메소드
     * @param req
     * @param res
     * @param failed
     */
    @Override
    protected  void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse res, AuthenticationException failed) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getOutputStream().write(objectMapper.writeValueAsString(ResponseResult.ofFailure(HttpStatus.UNAUTHORIZED, "로그인 실패")).getBytes());
        res.setStatus(401);
    }
}
