package com.ddd.mall.web.controller.auth;

import com.ddd.mall.application.command.auth.AdminLoginCommand;
import com.ddd.mall.application.command.auth.AdminLoginResult;
import com.ddd.mall.application.command.auth.AuthCommandHandler;
import com.ddd.mall.application.command.auth.MemberLoginCommand;
import com.ddd.mall.application.command.auth.MemberLoginResult;
import com.ddd.mall.web.request.auth.LoginRequest;
import com.ddd.mall.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 * 提供管理员和会员登录能力
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCommandHandler authCommandHandler;

    /**
     * 管理员登录
     *
     * @param request 登录请求参数
     * @return 管理员登录结果
     */
    @PostMapping("/admin/login")
    public ApiResponse<AdminLoginResult> adminLogin(@Valid @RequestBody LoginRequest request) {
        AdminLoginCommand command = new AdminLoginCommand(request.getUsername(), request.getPassword());
        return ApiResponse.ok(authCommandHandler.handle(command));
    }

    /**
     * 会员登录
     *
     * @param request 登录请求参数
     * @return 会员登录结果
     */
    @PostMapping("/member/login")
    public ApiResponse<MemberLoginResult> memberLogin(@Valid @RequestBody LoginRequest request) {
        MemberLoginCommand command = new MemberLoginCommand(request.getUsername(), request.getPassword());
        return ApiResponse.ok(authCommandHandler.handle(command));
    }
}
