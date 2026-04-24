package com.ddd.mall.application.command.auth;

import com.ddd.mall.domain.shared.TokenService;
import com.ddd.mall.domain.member.Member;
import com.ddd.mall.domain.member.MemberRepository;
import com.ddd.mall.domain.shared.DomainException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberLoginHandler {

    private final MemberRepository memberRepository;
    private final TokenService tokenService;

    public MemberLoginResult handle(MemberLoginCommand command) {
        Member member = memberRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new DomainException("用户名或密码错误"));

        if (!member.getPassword().equals(command.getPassword())) {
            throw new DomainException("用户名或密码错误");
        }

        String token = tokenService.generateMemberToken(member.getId(), member.getUsername());
        return new MemberLoginResult(token, member.getId(), member.getUsername(), member.getNickname());
    }

    @Getter
    @RequiredArgsConstructor
    public static class MemberLoginResult {
        private final String token;
        private final Long userId;
        private final String username;
        private final String nickname;
    }
}
