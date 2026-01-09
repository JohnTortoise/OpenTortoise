package io.github.johntortoise.core.dto.sys;

import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.model.Tool;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ConversationContext {
    private final List<Message> historyMessage;
    private final List<Message> toolCallMessage;
    private final Message input;
    private final Model modelConfig;
    private final String conversationId;
    private final long timestamp;
    private final Boolean stream;
    private final List<Tool> tools;
    private final Long maxToolDeep=3L;
}