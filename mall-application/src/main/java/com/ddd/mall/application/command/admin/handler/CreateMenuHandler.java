package com.ddd.mall.application.command.admin.handler;

import com.ddd.mall.application.command.admin.cmd.CreateMenuCommand;
import com.ddd.mall.domain.admin.Menu;
import com.ddd.mall.domain.admin.MenuRepository;
import com.ddd.mall.domain.admin.MenuType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateMenuHandler {

    private final MenuRepository menuRepository;

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