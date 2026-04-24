package com.ddd.mall.web.controller.admin;

import com.ddd.mall.application.command.admin.CreateMenuCommand;
import com.ddd.mall.application.command.admin.CreateMenuHandler;
import com.ddd.mall.application.query.admin.MenuTreeQueryHandler;
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

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
@RequireLogin(UserType.ADMIN)
public class MenuController {

    private final CreateMenuHandler createMenuHandler;
    private final MenuTreeQueryHandler menuTreeQueryHandler;

    @PostMapping
    @RequirePermission("menu:create")
    public ApiResponse<Long> createMenu(@Valid @RequestBody CreateMenuRequest request) {
        CreateMenuCommand command = new CreateMenuCommand(
                request.getName(), request.getParentId(),
                request.getPath(), request.getComponent(),
                request.getIcon(), request.getPermissionCode(),
                request.getType(), request.getSort());
        return ApiResponse.ok(createMenuHandler.handle(command));
    }

    @GetMapping("/tree")
    public ApiResponse<List<MenuTreeDto>> getMenuTree() {
        return ApiResponse.ok(menuTreeQueryHandler.handle());
    }
}
