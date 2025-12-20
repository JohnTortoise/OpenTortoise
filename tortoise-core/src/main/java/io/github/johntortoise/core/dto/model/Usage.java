package io.github.johntortoise.core.dto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Usage{
    @JsonProperty("completion_tokens")
    private Integer completionTokens;
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;
    @JsonProperty("total_tokens")
    private Integer totalTokens;
    @JsonProperty("prompt_tokens_details")
    private PromptTokensDetails promptTokensDetails;
    @JsonProperty("completion_tokens_details")
    private CompletionTokensDetails completionTokensDetails;
    @Data
    @NoArgsConstructor
    public static class PromptTokensDetails {
        @JsonProperty("cached_tokens")
        private Integer cachedTokens;
    }
    @Data
    @NoArgsConstructor
    public static class CompletionTokensDetails {
        @JsonProperty("reasoning_tokens")
        private Integer reasoningTokens;
    }
}