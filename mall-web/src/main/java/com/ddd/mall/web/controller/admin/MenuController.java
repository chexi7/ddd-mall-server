package com.ddd.mall.web.controller.admin;

import com.ddd.mall.application.command.admin.CreateMenuCommand;
import com.ddd.mall.application.command.admin.MenuCommandHandler;
import com.ddd.mall.application.query.admin.MenuQueryService;
import com.ddd.mall.application.query.admin.dto.MenuTreeDto;
import com.ddd.mall.infrastructure.auth.RequireLogin;
import com.ddd.mall.infrastructure.auth.RequirePermission;
import com.ddd.mall.infrastructure.auth.UserType;
import com.ddd.mall.web.request.admin.CreateMenuRequest;
import com.ddd.mall.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单接口
 * 提供菜单创建和菜单树查询能力
 */
@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
@RequireLogin(UserType.ADMIN)
public class MenuController {

    private final MenuCommandHandler menuCommandHandler;
    private final MenuQueryService menuQueryService;

    /**
     * 创建菜单
     *
     * @param request 创建菜单请求参数
     * @return 创建的菜单ID
     */
    @PostMapping
    @RequirePermission("menu:create")
    public ApiResponse<Long> createMenu(@Valid @RequestBody CreateMenuRequest request) {
        CreateMenuCommand command = new CreateMenuCommand(
                request.getName(), request.getParentId(),
                request.getPath(), request.getComponent(),
                request.getIcon(), request.getPermissionCode(),
                request.getType(), request.getSort());
        return ApiResponse.ok(menuCommandHandler.handle(command));
    }

    /**
     * 查询菜单树
     *
     * @return 菜单树列表
     */
    @GetMapping("/tree")
    public ApiResponse<List<MenuTreeDto>> getMenuTree() {
        return ApiResponse.ok(menuQueryService.menuTree());
    }
}
