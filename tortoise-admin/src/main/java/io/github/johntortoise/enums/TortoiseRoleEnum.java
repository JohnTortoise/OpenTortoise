package io.github.johntortoise.enums;

import lombok.Getter;


@Getter
public enum TortoiseRoleEnum {


    CUSTOMER(0L, "customer", "消费者", "服务的客户"),

    SUPER_ADMIN(1L, "super_admin", "超级管理员", "系统超级管理员，拥有所有权限"),
    DEVELOPER(2L, "developer", "开发", "开发人员角色"),
    TESTER(3L, "tester", "测试", "测试人员角色"),
    PRODUCT(4L, "product", "产品", "产品经理角色"),
    OPERATION(5L, "operation", "运营", "运营人员角色");

    private final Long id;
    private final String roleCode;
    private final String roleName;
    private final String description;

    TortoiseRoleEnum(Long id, String roleCode, String roleName, String description) {
        this.id = id;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.description = description;
    }


    public static TortoiseRoleEnum getById(Long id) {
        for (TortoiseRoleEnum role : values()) {
            if (role.getId().equals(id)) {
                return role;
            }
        }
        return null;
    }


    public static TortoiseRoleEnum getByRoleCode(String roleCode) {
        for (TortoiseRoleEnum role : values()) {
            if (role.getRoleCode().equals(roleCode)) {
                return role;
            }
        }
        return null;
    }


    public static boolean containsId(Long id) {
        return getById(id) != null;
    }


    public static boolean containsRoleCode(String roleCode) {
        return getByRoleCode(roleCode) != null;
    }
}