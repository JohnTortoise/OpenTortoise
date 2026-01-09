package io.github.johntortoise.core.dto.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatCompletionRequest {
    private String model;
    private List<Message> messages;
    private BigDecimal temperature;
    private Boolean stream;
    private StreamOptions stream_options;
    private List<Tool> tools;

    public ChatCompletionRequest(String modelName, List<Message> messages, BigDecimal temperature, boolean stream, boolean includeUsage,List<Tool> tools) {
        StreamOptions streamOptions = new StreamOptions();
        streamOptions.setInclude_usage(includeUsage);
        this.model = modelName;
        this.messages = messages;
        this.temperature = temperature;
        this.stream = stream;
        this.stream_options = streamOptions;
        this.tools = tools;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class  StreamOptions{
        private Boolean include_usage;
    }
    

}

