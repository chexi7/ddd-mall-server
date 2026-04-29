package com.ddd.mall.application.command.admin;

import com.ddd.mall.domain.menu.Menu;
import com.ddd.mall.domain.menu.MenuRepository;
import com.ddd.mall.domain.menu.MenuType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 菜单聚合命令处理器，处理菜单相关命令。
 * <p>
 * 对应 DDD 铁律：一个命令处理器只操作一个聚合，聚合仅可以在命令处理器中进行操作。
 */
@Service
@RequiredArgsConstructor
public class MenuCommandHandler {

    private final MenuRepository menuRepository;

    /**
     * 处理创建菜单命令。
     */
    @Transactional
    public Long handle(CreateMenuCommand command) {
        Menu menu = new Menu(
                command.getName(), command.getParentId(),
                command.getPath(), command.getComponent(),
                command.getIcon(), command.getPermissionCode(),
                MenuType.valueOf(command.getType()), command.getSort());
        menuRepository.save(menu);
        return menu.getId();
    }
}