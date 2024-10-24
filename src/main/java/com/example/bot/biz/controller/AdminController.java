package com.example.bot.biz.controller;

import com.example.bot.biz.dto.JoinDTO;
import com.example.bot.biz.service.AdminService;
import com.example.bot.core.config.ResponseResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 회원 가입
     *
     * @param joinDTO p1
     * @return ResponseResult<?>
     */
    @PostMapping("/signup")
    public ResponseResult<?> signup(@Valid @RequestBody JoinDTO joinDTO) {
        adminService.signup(joinDTO);

        return ResponseResult.ofSuccess("success", null);
    }
}
