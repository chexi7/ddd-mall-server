package com.ddd.mall.web.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分配权限请求参数
 */
@Getter
@Setter
public class AssignPermissionsRequest {
    /**
     * 权限编码列表
     */
    @NotNull(message = "权限编码列表不能为空")
    private List<String> permissionCodes;
}
