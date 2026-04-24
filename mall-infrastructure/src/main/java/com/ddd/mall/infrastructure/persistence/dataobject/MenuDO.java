package com.ddd.mall.infrastructure.persistence.dataobject;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "menus")
public class MenuDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String name;

    private Long parentId;

    @Column(length = 200)
    private String path;

    @Column(length = 200)
    private String component;

    @Column(length = 50)
    private String icon;

    @Column(length = 100)
    private String permissionCode;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false)
    private Integer sort;

    @Column(nullable = false)
    private Boolean visible;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
