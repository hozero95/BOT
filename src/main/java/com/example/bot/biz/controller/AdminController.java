package com.example.bot.biz.controller;

import com.example.bot.biz.dto.admin.ResetPasswordDTO;
import com.example.bot.biz.dto.admin.SignupDTO;
import com.example.bot.biz.service.AdminService;
import com.example.bot.core.config.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "Admin 관련 API")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 유저 생성
     *
     * @param signupDTO p1
     * @return ResponseResult<?>
     */
    @Operation(summary = "유저 생성")
    @PostMapping("/signup")
    public ResponseResult<?> signup(@Valid @RequestBody SignupDTO.Request signupDTO) {
        adminService.signup(signupDTO);

        return ResponseResult.ofSuccess("success", null);
    }

    /**
     * 비밀번호 초기화
     *
     * @param resetPasswordDTO p1
     * @return ResponseResult<?>
     */
    @Operation(summary = "비밀번호 초기화")
    @PostMapping("/reset-password")
    public ResponseResult<?> resetPassword(@Valid @RequestBody ResetPasswordDTO.Request resetPasswordDTO) {
        return ResponseResult.ofSuccess("success", null);
    }
}
