package com.example.bot.biz.controller;

import com.example.bot.biz.dto.JoinDTO;
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
     * @param joinDTO p1
     * @return ResponseResult<?>
     */
    @Operation(summary = "유저 생성")
    @PostMapping("/signup")
    public ResponseResult<?> signup(@Valid @RequestBody JoinDTO joinDTO) {
        adminService.signup(joinDTO);

        return ResponseResult.ofSuccess("success", null);
    }
}
