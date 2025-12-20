package io.github.johntortoise.enums;


public enum DeletedEnum {
    NO_EXIST(1,"删除"),
    EXIST( 0,"存在"),
    ;

    private final int code;
    private final String desc;

    DeletedEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }


    public static DeletedEnum getByCode(Integer code) {
        for (DeletedEnum enabelEnum : values()) {
            if (enabelEnum.getCode()==code) {
                return enabelEnum;
            }
        }
        return null;
    }

}