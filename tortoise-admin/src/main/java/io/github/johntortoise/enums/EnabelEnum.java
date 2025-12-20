package io.github.johntortoise.enums;


public enum EnabelEnum {
    OPEN(1,"启用"),
    CLOSE( 0,"禁用"),
    ;

    private final int code;
    private final String desc;

    EnabelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }


    public static EnabelEnum getByCode(Integer code) {
        for (EnabelEnum enabelEnum : values()) {
            if (enabelEnum.getCode()==code) {
                return enabelEnum;
            }
        }
        return null;
    }

}