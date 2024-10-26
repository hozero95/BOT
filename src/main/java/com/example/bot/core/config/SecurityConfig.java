package com.example.bot.core.config;

import com.example.bot.biz.repository.AuthRepository;
import com.example.bot.biz.repository.RefreshRepository;
import com.example.bot.core.security.filter.JwtFilter;
import com.example.bot.core.security.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;

/**
 * Security Config
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final AuthRepository authRepository;

    public SecurityConfig(JwtUtil jwtUtil, RefreshRepository refreshRepository, AuthRepository authRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.authRepository = authRepository;
    }

    /**
     * AuthenticationManager 등록 Bean
     *
     * @param authenticationConfiguration p1
     * @return AuthenticationManager
     * @throws Exception e1
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Password Hash 암호화 Bean
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 통신 Security Filter 설정
     *
     * @param http p1
     * @return SecurityFilterChain
     * @throws Exception e1
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RequestMatcherHolder requestMatcherHolder = new RequestMatcherHolder();

        // 0. CORS 설정
        http.cors((cors) -> cors.configurationSource(request -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowCredentials(true);
            configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
            configuration.setAllowedMethods(Collections.singletonList("*"));
            configuration.setAllowedHeaders(Collections.singletonList("*"));
            configuration.setMaxAge(3600L);
            configuration.setExposedHeaders(Collections.singletonList("Authorization"));
            return configuration;
        }));

        // 1. csrf disable
        // Stateless Session 방식으로 인해 CSRF 공격 방어할 필요가 없으므로 해당 기능 disable
        http.csrf(AbstractHttpConfigurer::disable);

        // 2. Form 로그인 방식 disable
        // REST 방식의 JWT 로그인을 사용할 것이므로 Form 로그인 접근 제한
        http.formLogin(AbstractHttpConfigurer::disable);

        // 3. Http Basic 인증 방식 disable
        // REST 방식의 JWT 로그인을 사용할 것이므로 HTTP Basic 로그인 접근 제한
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 4. 경로별 인가 작업
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers(
                        requestMatcherHolder.getPERMIT_ALL_URLS().toArray(new String[0])
                ).permitAll()
                .requestMatchers(
                        requestMatcherHolder.getPERMIT_USER_URLS().toArray(new String[0])
                ).hasAnyRole("USER", "ADMIN")
                .requestMatchers(
                        requestMatcherHolder.getPERMIT_ADMIN_URLS().toArray(new String[0])
                ).hasRole("ADMIN")
                .anyRequest().authenticated());

        // 5. 필터 추가
        // 로그인, 로그아웃 필터 => REST API 대체
//        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JwtFilter(jwtUtil, authRepository, refreshRepository), UsernamePasswordAuthenticationFilter.class);
//        http.addFilterBefore(new LogoutFilter(jwtUtil, refreshRepository), org.springframework.security.web.authentication.logout.LogoutFilter.class);

        // 6. 세션 설정
        // Session Stateless 설정
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}