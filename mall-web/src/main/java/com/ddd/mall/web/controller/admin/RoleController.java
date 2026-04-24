package com.ddd.mall.web.controller.admin;

import com.ddd.mall.application.command.admin.AssignPermissionsCommand;
import com.ddd.mall.application.command.admin.AssignPermissionsHandler;
import com.ddd.mall.application.command.admin.CreateRoleCommand;
import com.ddd.mall.application.command.admin.CreateRoleHandler;
import com.ddd.mall.infrastructure.auth.RequireLogin;
import com.ddd.mall.infrastructure.auth.RequirePermission;
import com.ddd.mall.infrastructure.auth.UserType;
import com.ddd.mall.web.request.admin.AssignPermissionsRequest;
import com.ddd.mall.web.request.admin.CreateRoleRequest;
import com.ddd.mall.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@RequireLogin(UserType.ADMIN)
public class RoleController {

    private final CreateRoleHandler createRoleHandler;
    private final AssignPermissionsHandler assignPermissionsHandler;

    @PostMapping
    @RequirePermission("role:create")
    public ApiResponse<Long> createRole(@Valid @RequestBody CreateRoleRequest request) {
        CreateRoleCommand command = new CreateRoleCommand(
                request.getName(), request.getCode(), request.getDescription());
        return ApiResponse.ok(createRoleHandler.handle(command));
    }

    @PutMapping("/{id}/permissions")
    @RequirePermission("role:assign-permission")
    public ApiResponse<Void> assignPermissions(@PathVariable Long id,
                                                @Valid @RequestBody AssignPermissionsRequest request) {
        assignPermissionsHandler.handle(new AssignPermissionsCommand(id, request.getPermissionCodes()));
        return ApiResponse.ok();
    }
}
