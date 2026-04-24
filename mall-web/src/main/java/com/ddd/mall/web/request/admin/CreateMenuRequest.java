package com.ddd.mall.web.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMenuRequest {
    @NotBlank(message = "菜单名称不能为空")
    private String name;
    private Long parentId;
    private String path;
    private String component;
    private String icon;
    private String permissionCode;
    @NotNull(message = "菜单类型不能为空")
    private String type;
    private Integer sort;
}
