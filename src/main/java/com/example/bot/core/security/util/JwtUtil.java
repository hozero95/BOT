package com.example.bot.core.security.util;

import com.example.bot.biz.entity.RefreshToken;
import com.example.bot.biz.repository.RefreshRepository;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Jwt Util
 */
@Slf4j
@Component
public class JwtUtil {
    private final RefreshRepository refreshRepository;

    private final SecretKey secretKey;
    public static final Long ACCESS_TOKEN_EXPIRE = 60 * 10 * 1000L; // Access Token Expired: 10분
    public static final Long REFRESH_TOKEN_EXPIRE = 60 * 1440 * 1000L; // Refresh Token Expired: 1440분(24시간)

    public JwtUtil(RefreshRepository refreshRepository, @Value("${spring.jwt.secret}") String secretKey) {
        this.refreshRepository = refreshRepository;
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * Token 내부 Username 검증하는 메소드 => 이 프로젝트에서는 Email
     *
     * @param token p1
     * @return String
     */
    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    /**
     * Token 내부 Role 검증하는 메소드
     *
     * @param token p1
     * @return String
     */
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    /**
     * Token 내부 Category 검증하는 메소드
     *
     * @param token p1
     * @return String
     */
    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    /**
     * Token 만료되었는지 검증하는 메소드
     *
     * @param token p1
     */
    public void isExpired(String token) {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
    }

    /**
     * 로그인 성공 시 Token 생성하는 메소드
     *
     * @param category  p1
     * @param username  p2
     * @param role      p3
     * @param expiredMs p4
     * @return String
     */
    public String createJwt(String category, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 을 담을 쿠키 생성 메소드
     *
     * @param value p2
     * @return Cookie
     */
    public Cookie createCookie(String value) {
        Cookie cookie = new Cookie("Refresh-Token", value);
        cookie.setMaxAge(Math.toIntExact(JwtUtil.REFRESH_TOKEN_EXPIRE)); // 60분
//        cookie.setSecure(true); // HTTPS 통신 시 활성화
//        cookie.setPath("/"); // 쿠키가 적용될 범위
        cookie.setHttpOnly(true); // 클라이언트에서 JavaScript 코드를 통한 Cookie 접근 제한
        return cookie;
    }

    /**
     * Refresh Token 저장
     *
     * @param email     p1
     * @param token     p2
     * @param expiredMs p3
     */
    public void addRefreshToken(String email, String token, Long expiredMs) {
        LocalDateTime now = LocalDateTime.now();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail(email);
        refreshToken.setToken(token);
        refreshToken.setCreateDt(now);
        refreshToken.setExpiredDt(now.plusSeconds(expiredMs / 1000));

        refreshRepository.save(refreshToken);
    }
}
