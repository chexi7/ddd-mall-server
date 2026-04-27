package com.ddd.mall.web.controller.admin;

import com.ddd.mall.application.command.admin.AdminApplicationService;
import com.ddd.mall.application.command.admin.AssignRolesCommand;
import com.ddd.mall.application.command.admin.CreateAdminCommand;
import com.ddd.mall.application.query.admin.AdminQueryService;
import com.ddd.mall.application.query.admin.dto.AdminListItemDto;
import com.ddd.mall.application.query.support.PageResult;
import com.ddd.mall.infrastructure.auth.RequireLogin;
import com.ddd.mall.infrastructure.auth.RequirePermission;
import com.ddd.mall.infrastructure.auth.UserType;
import com.ddd.mall.web.request.admin.AssignRolesRequest;
import com.ddd.mall.web.request.admin.CreateAdminRequest;
import com.ddd.mall.web.response.ApiResponse;
import com.ddd.mall.web.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员接口
 * 提供管理员创建和角色分配能力
 */
@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
@RequireLogin(UserType.ADMIN)
public class AdminController {

    private final AdminApplicationService adminApplicationService;
    private final AdminQueryService adminQueryService;

    /**
     * 分页查询管理员列表
     */
    @GetMapping
    public ApiResponse<PageResponse<AdminListItemDto>> listAdmins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<AdminListItemDto> r = adminQueryService.adminList(page, size, keyword);
        return ApiResponse.ok(new PageResponse<>(
                r.getContent(), r.getTotalElements(), r.getTotalPages(), r.getPage(), r.getSize()));
    }

    /**
     * 创建管理员
     *
     * @param request 创建管理员请求参数
     * @return 创建的管理员ID
     */
    @PostMapping
    @RequirePermission("admin:create")
    public ApiResponse<Long> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        CreateAdminCommand command = new CreateAdminCommand(
                request.getUsername(), request.getPassword(), request.getRealName(),
                request.getPhone(), request.getEmail());
        return ApiResponse.ok(adminApplicationService.createAdmin(command));
    }

    /**
     * 分配管理员角色
     *
     * @param id 管理员ID
     * @param request 角色分配请求参数
     * @return 空响应
     */
    @PutMapping("/{id}/roles")
    @RequirePermission("admin:assign-role")
    public ApiResponse<Void> assignRoles(@PathVariable Long id,
                                         @Valid @RequestBody AssignRolesRequest request) {
        adminApplicationService.assignRoles(new AssignRolesCommand(id, request.getRoleIds()));
        return ApiResponse.ok();
    }
}
