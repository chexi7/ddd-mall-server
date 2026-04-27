package com.ddd.mall.web.controller.admin;

import com.ddd.mall.infrastructure.auth.RequireLogin;
import com.ddd.mall.infrastructure.auth.UserType;
import com.ddd.mall.web.response.ApiResponse;
import com.ddd.mall.web.response.admin.DashboardStatsDto;
import com.ddd.mall.web.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端仪表盘
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@RequireLogin(UserType.ADMIN)
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsDto> stats() {
        return ApiResponse.ok(dashboardStatsService.load());
    }
}
