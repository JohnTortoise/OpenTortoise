package io.github.johntortoise.core.dto.sys;

import io.github.johntortoise.core.dto.model.ToolCall;
import io.github.johntortoise.core.dto.model.Usage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessedStreamResult {
    private String content;
    private String role;
    private Usage usage;
    private ToolCall toolCall;
}