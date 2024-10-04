package com.example.bot.core.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestMatcherHolder {
    private final List<String> PERMIT_ALL_URLS = List.of(
            "/login",
            "/api/test/**",
            "/api/auth/**"
    );
    private final List<String> PERMIT_ADMIN_URLS = List.of(
            "/api/admin/**"
    );
}
