package io.github.johntortoise.enums;


public enum MemoryTypeEnum {
    RECENT(1,"截断"),
    SUMMARY( 2,"摘要"),
    TIME_MAP(3, "时序图谱"),
    ;

    private final int code;
    private final String desc;

    MemoryTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc(){return desc;}


    public static MemoryTypeEnum getByCode(Integer code) {
        for (MemoryTypeEnum memoryEnum : values()) {
            if (memoryEnum.getCode()==code) {
                return memoryEnum;
            }
        }
        return null;
    }

}