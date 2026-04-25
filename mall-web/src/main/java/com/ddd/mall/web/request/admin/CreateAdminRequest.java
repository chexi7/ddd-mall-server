package com.ddd.mall.web.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建管理员请求参数
 */
@Getter
@Setter
public class CreateAdminRequest {
    /**
     * 管理员用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 管理员密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱地址
     */
    private String email;
}
