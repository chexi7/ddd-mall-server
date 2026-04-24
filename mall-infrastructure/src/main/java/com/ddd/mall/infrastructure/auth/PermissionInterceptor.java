package com.ddd.mall.infrastructure.auth;

import com.ddd.mall.domain.shared.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 权限拦截器
 * 检查 @RequireLogin 和 @RequirePermission 注解
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 检查 @RequireLogin
        RequireLogin requireLogin = handlerMethod.getMethodAnnotation(RequireLogin.class);
        if (requireLogin == null) {
            requireLogin = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
        }

        if (requireLogin != null) {
            LoginUser currentUser = SecurityContext.getCurrentUser();
            if (currentUser == null) {
                throw new DomainException("请先登录");
            }

            // 检查用户类型
            UserType[] allowedTypes = requireLogin.value();
            if (allowedTypes.length > 0) {
                boolean typeAllowed = Arrays.asList(allowedTypes).contains(currentUser.getUserType());
                if (!typeAllowed) {
                    throw new DomainException("无权访问");
                }
            }
        }

        // 检查 @RequirePermission
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission != null) {
            LoginUser currentUser = SecurityContext.getCurrentUser();
            if (currentUser == null) {
                throw new DomainException("请先登录");
            }
            if (!currentUser.hasPermission(requirePermission.value())) {
                throw new DomainException("权限不足: " + requirePermission.value());
            }
        }

        return true;
    }
}
