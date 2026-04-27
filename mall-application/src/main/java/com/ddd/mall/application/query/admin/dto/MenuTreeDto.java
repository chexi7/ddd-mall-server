package com.ddd.mall.application.query.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MenuTreeDto {
    private Long id;
    private String name;
    private Long parentId;
    private String path;
    private String component;
    private String icon;
    private String permissionCode;
    private String type;
    private Integer sort;
    /**
     * 与前端字段 orderNum 对齐，取值同 sort
     */
    private Integer orderNum;
    private Boolean visible;
    private String createdAt;
    private List<MenuTreeDto> children = new ArrayList<>();
}
