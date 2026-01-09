package io.github.johntortoise.core.dto.sys;

import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.core.dto.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AfterChatDTO {
    private String conversationId;
    private MainInfo mainInfo;

    private List<LLmInvokeResp.AllChat> allChatInfo;
    private List<Message> toolCallMessages;
    private List<LLmInvokeResp.Event> events;

}
