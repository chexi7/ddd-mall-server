package com.ddd.mall.web.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignPermissionsRequest {
    @NotNull(message = "权限编码列表不能为空")
    private List<String> permissionCodes;
}
