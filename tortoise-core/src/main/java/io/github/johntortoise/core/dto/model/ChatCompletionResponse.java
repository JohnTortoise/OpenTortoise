package io.github.johntortoise.core.dto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatCompletionResponse {
    private Choice[] choices;
    private Long created;
    private String id;
    private String model;

    @JsonProperty("service_tier")
    private String serviceTier;

    private String object;
    private Usage usage;
    private String error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        @JsonProperty("finish_reason")
        private String finishReason;

        private Integer index;
        private Object logprobs;
        private Message message;

        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }

    @Data
    public static class Message extends io.github.johntortoise.core.dto.model.Message{
        @JsonProperty("reasoning_content")
        private String reasoningContent;

        private String type;

        @JsonProperty("tool_calls")
        private List<ToolCall> toolCalls;

        @JsonProperty("tool_call_id")
        private String toolCallId;

    }



}