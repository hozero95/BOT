package com.example.bot.core.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 페이지별 권한 설정
 */
@Getter
@Setter
public class RequestMatcherHolder {
    private final List<String> PERMIT_ALL_URLS = List.of(
            "/login",
            "/api/test/**"
    );
    private final List<String> PERMIT_USER_URLS = List.of(
            "/api/token/**",
            "/api/order/**"
    );
    private final List<String> PERMIT_ADMIN_URLS = List.of(
            "/api/admin/**"
    );
}
