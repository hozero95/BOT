package com.example.bot.config;

import com.example.bot.jwt.JwtFilter;
import com.example.bot.jwt.JwtUtil;
import com.example.bot.jwt.LoginFilter;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JwtUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    /**
     * AuthenticationManager 등록 Bean
     * @param authenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Password Hash 암호화 Bean
     * @return BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 통신 Security Filter 설정
     * @param http
     * @return SecurityFilterChain
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                .requestMatchers("/login", "/api/test/**", "/api/auth/**").permitAll()
//                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());

        // 5. 필터 추가
        // JwtFilter 가 LoginFilter 뒤에서 필터링되도록 설정 => 특정 케이스에서 Login Filter 보다 JWT Filter 가 먼저 실행될 때 발생하는 오류가 있음
        http.addFilterAfter(new JwtFilter(jwtUtil), LoginFilter.class);
        // LoginFilter 는 AuthenticationConfiguration 객체를 인자로 받은 AuthenticationManager 를 인자로 받음 (즉, LoginFilter(AuthenticationManager(AuthenticationConfiguration)) 형식)
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 6. 세션 설정
        // Session Stateless 설정
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}