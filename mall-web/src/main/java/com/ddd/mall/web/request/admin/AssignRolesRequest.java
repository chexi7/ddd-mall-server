package com.ddd.mall.web.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分配角色请求参数
 */
@Getter
@Setter
public class AssignRolesRequest {
    /**
     * 角色ID列表
     */
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
