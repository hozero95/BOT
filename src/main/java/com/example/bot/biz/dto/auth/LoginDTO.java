package com.example.bot.biz.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 로그인 DTO
 */
@SuppressWarnings("SpellCheckingInspection")
public class LoginDTO {
    @Data
    public static class Request {
        @NotNull
        private String usercd;

        @NotNull
        private String password;
    }

    @Data
    public static class Response {
        private String usercd;

        private String usernm;
    }
}
