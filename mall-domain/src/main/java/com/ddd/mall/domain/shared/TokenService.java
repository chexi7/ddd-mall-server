package com.ddd.mall.domain.shared;

import java.util.List;
import java.util.Set;

/**
 * Token 服务接口（应用层定义，基础设施层实现）
 * 遵循依赖倒置：应用层不依赖具体的 JWT 实现
 */
public interface TokenService {

    /**
     * 为管理员生成 Token
     */
    String generateAdminToken(Long userId, String username, List<String> roles, Set<String> permissions);

    /**
     * 为 C 端用户生成 Token
     */
    String generateMemberToken(Long userId, String username);
}
