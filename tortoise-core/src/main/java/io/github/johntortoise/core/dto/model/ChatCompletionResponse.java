package io.github.johntortoise.core.dto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String content;
        private String role;

        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }

}