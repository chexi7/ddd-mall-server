package com.ddd.mall.infrastructure.persistence.dataobject;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "members")
public class MemberDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 20)
    private String phone;

    // 地址（扁平化存储）
    @Column(length = 20)
    private String province;
    @Column(length = 20)
    private String city;
    @Column(length = 20)
    private String district;
    @Column(length = 200)
    private String addressDetail;
    @Column(length = 10)
    private String zipCode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
