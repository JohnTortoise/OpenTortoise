package io.github.johntortoise.dto;

import io.github.johntortoise.core.dto.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteMessagesReq {

    private String conversationId;

    private List<Message> messages;
}
