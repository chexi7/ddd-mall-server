package com.ddd.mall.web.controller.auth;

import com.ddd.mall.application.command.auth.AdminLoginCommand;
import com.ddd.mall.application.command.auth.AdminLoginHandler;
import com.ddd.mall.application.command.auth.MemberLoginCommand;
import com.ddd.mall.application.command.auth.MemberLoginHandler;
import com.ddd.mall.web.request.auth.LoginRequest;
import com.ddd.mall.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminLoginHandler adminLoginHandler;
    private final MemberLoginHandler memberLoginHandler;

    /**
     * 后台管理员登录
     */
    @PostMapping("/admin/login")
    public ApiResponse<AdminLoginHandler.LoginResult> adminLogin(@Valid @RequestBody LoginRequest request) {
        AdminLoginCommand command = new AdminLoginCommand(request.getUsername(), request.getPassword());
        return ApiResponse.ok(adminLoginHandler.handle(command));
    }

    /**
     * C端会员登录
     */
    @PostMapping("/member/login")
    public ApiResponse<MemberLoginHandler.MemberLoginResult> memberLogin(@Valid @RequestBody LoginRequest request) {
        MemberLoginCommand command = new MemberLoginCommand(request.getUsername(), request.getPassword());
        return ApiResponse.ok(memberLoginHandler.handle(command));
    }
}
