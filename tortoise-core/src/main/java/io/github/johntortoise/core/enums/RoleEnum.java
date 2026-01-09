package io.github.johntortoise.core.enums;

public enum RoleEnum {
    
    SYSTEM("system", "系统"),
    USER("user", "用户"),
    ASSISTANT("assistant","助手"),
    TOOL("tool","工具")

 ;
    
    private final String code;
    private final String description;
    
    RoleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    

    public static RoleEnum getByCode(String code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
    

    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}