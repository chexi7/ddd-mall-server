package com.ddd.mall.application.command.admin;

import com.ddd.mall.domain.menu.Menu;
import com.ddd.mall.domain.menu.MenuRepository;
import com.ddd.mall.domain.menu.MenuType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 菜单聚合应用服务，承接菜单相关业务用例。
 */
@Service
@RequiredArgsConstructor
public class MenuApplicationService {

    /**
     * 菜单仓储
     */
    private final MenuRepository menuRepository;

    /**
     * 创建菜单。
     *
     * @param command 创建菜单命令
     * @return 新建菜单 ID
     */
    @Transactional
    public Long createMenu(CreateMenuCommand command) {
        Menu menu = new Menu(
                command.getName(), command.getParentId(),
                command.getPath(), command.getComponent(),
                command.getIcon(), command.getPermissionCode(),
                MenuType.valueOf(command.getType()), command.getSort());
        menuRepository.save(menu);
        return menu.getId();
    }
}