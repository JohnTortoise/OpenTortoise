package io.github.johntortoise.core.dto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatChunk {

    @JsonProperty("choices")
    private List<Choice> choices;

    @JsonProperty("created")
    private Long created;

    @JsonProperty("id")
    private String id;

    @JsonProperty("model")
    private String model;

    @JsonProperty("service_tier")
    private String serviceTier;

    @JsonProperty("object")
    private String object;

    @JsonProperty("usage")
    private Usage usage;

    @Data
    @NoArgsConstructor
    public static class Choice {

        @JsonProperty("delta")
        private Delta delta;

        @JsonProperty("index")
        private Integer index;
    }

    @Data
    @NoArgsConstructor
    public static class Delta {

        @JsonProperty("content")
        private String content;

        @JsonProperty("role")
        private String role;
    }
}