package io.github.johntortoise.core.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MainInfo {

    private List<Message> history;

    private Message input;

    private Message outPut;

    private Tokens tokens;


    @Data
    public static class Tokens{
        private Integer completionTokens = 0;

        private Integer promptTokens = 0 ;

        private Integer totalTokens = 0;

        private Integer cachedTokens = 0 ;

    }
}
