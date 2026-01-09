package io.github.johntortoise.core.dto.model;

import lombok.Data;

@Data
public  class ToolCall{

    private Function function;

    private String id;

    private String type;

    @Data
    public static class Function{
        private String arguments;
        private String name;
    }

}
