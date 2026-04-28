package com.ddd.mall.domain.member;

import com.ddd.mall.domain.shared.AggregateRoot;
import com.ddd.mall.domain.shared.DomainException;
import com.ddd.mall.domain.shared.ReconstructionOnly;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会员聚合根
 */
@Getter
@ReconstructionOnly
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends AggregateRoot {

    /**
     * 会员登录名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 收货地址
     */
    private Address address;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

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