package com.ddd.mall.web.controller.admin;

import com.ddd.mall.application.command.admin.AssignPermissionsCommand;
import com.ddd.mall.application.command.admin.AssignPermissionsHandler;
import com.ddd.mall.application.command.admin.CreateRoleCommand;
import com.ddd.mall.application.command.admin.CreateRoleHandler;
import com.ddd.mall.application.query.admin.RoleListQueryHandler;
import com.ddd.mall.application.query.admin.dto.RoleListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.infrastructure.auth.RequireLogin;
import com.ddd.mall.infrastructure.auth.RequirePermission;
import com.ddd.mall.infrastructure.auth.UserType;
import com.ddd.mall.web.request.admin.AssignPermissionsRequest;
import com.ddd.mall.web.request.admin.CreateRoleRequest;
import com.ddd.mall.web.response.ApiResponse;
import com.ddd.mall.web.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 角色接口
 * 提供角色创建和权限分配能力
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@RequireLogin(UserType.ADMIN)
public class RoleController {

    private final CreateRoleHandler createRoleHandler;
    private final AssignPermissionsHandler assignPermissionsHandler;
    private final RoleListQueryHandler roleListQueryHandler;

    /**
     * 分页查询角色列表
     */
    @GetMapping
    public ApiResponse<PageResponse<RoleListItemDto>> listRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<RoleListItemDto> r = roleListQueryHandler.handle(page, size);
        return ApiResponse.ok(new PageResponse<>(
                r.getContent(), r.getTotalElements(), r.getTotalPages(), r.getPage(), r.getSize()));
    }

    /**
     * 创建角色
     *
     * @param request 创建角色请求参数
     * @return 创建的角色ID
     */
    @PostMapping
    @RequirePermission("role:create")
    public ApiResponse<Long> createRole(@Valid @RequestBody CreateRoleRequest request) {
        CreateRoleCommand command = new CreateRoleCommand(
                request.getName(), request.getCode(), request.getDescription());
        return ApiResponse.ok(createRoleHandler.handle(command));
    }

    /**
     * 分配角色权限
     *
     * @param id 角色ID
     * @param request 权限分配请求参数
     * @return 空响应
     */
    @PutMapping("/{id}/permissions")
    @RequirePermission("role:assign-permission")
    public ApiResponse<Void> assignPermissions(@PathVariable Long id,
                                               @Valid @RequestBody AssignPermissionsRequest request) {
        assignPermissionsHandler.handle(new AssignPermissionsCommand(id, request.getPermissionCodes()));
        return ApiResponse.ok();
    }
}
