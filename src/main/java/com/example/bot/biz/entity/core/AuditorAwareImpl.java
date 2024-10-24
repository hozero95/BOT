package com.example.bot.biz.entity.core;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AuditorAware
 */
@SuppressWarnings("NullableProblems")
@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        String username = "system";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }

        // 현재 사용자를 반환 (예: 인증된 사용자 또는 시스템 계정)
        return Optional.of(username.equals("anonymousUser") ? "system" : username); // 비로그인 시 임시로 "system"을 반환
    }
}
