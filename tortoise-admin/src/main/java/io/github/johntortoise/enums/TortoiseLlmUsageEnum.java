package io.github.johntortoise.enums;

import lombok.Getter;


@Getter
public enum TortoiseLlmUsageEnum {

    CONVERSATION(0, "CONVERSATION", "会话", "对话产生的成本消耗类型"),

    MEMORY_BASE(1, "MEMORY_BASE", "记忆生成", "使用基础提示词进行分析时产生的成本"),

    MEMORY_UPDATE(2,"MEMORY_UPDATE","记忆更新","使用更新提示词进行分析时产生的成本"),

    MEMORY_OVER_LENGTH(3,"MEMORY_OVER_LENGTH","记忆精简","记忆长度超限时进行精简产生的成本")

            ;

    private final Integer id;
    private final String code;
    private final String name;
    private final String description;

    TortoiseLlmUsageEnum(Integer id, String code, String name, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
    }


    public static TortoiseLlmUsageEnum getById(Integer id) {
        for (TortoiseLlmUsageEnum usageEnum : values()) {
            if (usageEnum.getId().equals(id)) {
                return usageEnum;
            }
        }
        return null;
    }


    public static TortoiseLlmUsageEnum getByCode(String code) {
        for (TortoiseLlmUsageEnum usageEnum : values()) {
            if (usageEnum.getCode().equals(code)) {
                return usageEnum;
            }
        }
        return null;
    }


}