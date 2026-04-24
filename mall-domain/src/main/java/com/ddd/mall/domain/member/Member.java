package com.ddd.mall.domain.member;

import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 会员聚合根
 */
@Getter
@Setter
public class Member extends AggregateRoot {

    private String username;
    private String password;
    private String nickname;
    private String phone;
    private Address address;
    private LocalDateTime createdAt;

    protected Member() {}

    public Member(String username, String password, String nickname) {
        if (username == null || username.isBlank()) throw new DomainException("用户名不能为空");
        if (password == null || password.length() < 6) throw new DomainException("密码长度不能少于6位");
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.createdAt = LocalDateTime.now();
    }

    public void updateProfile(String nickname, String phone) {
        this.nickname = nickname;
        this.phone = phone;
    }

    public void updateAddress(Address address) {
        this.address = address;
    }
}
