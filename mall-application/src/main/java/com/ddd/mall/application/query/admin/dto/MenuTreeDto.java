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
    private Boolean visible;
    private List<MenuTreeDto> children = new ArrayList<>();
}
