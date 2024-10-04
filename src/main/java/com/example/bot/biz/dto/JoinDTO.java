package com.example.bot.biz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinDTO {
    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String name;
}
