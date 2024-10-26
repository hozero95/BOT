package com.example.bot.biz.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@SuppressWarnings("SpellCheckingInspection")
public class ResetPasswordDTO {
    @Data
    public static class Request {
        @NotNull
        private String usercd;

        @NotNull
        private String password;
    }
}
