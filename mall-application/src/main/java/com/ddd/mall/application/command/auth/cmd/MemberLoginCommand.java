package com.ddd.mall.application.command.auth.cmd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberLoginCommand {
    private final String username;
    private final String password;
}