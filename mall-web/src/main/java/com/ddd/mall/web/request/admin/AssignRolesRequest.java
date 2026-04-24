package com.ddd.mall.web.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignRolesRequest {
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
