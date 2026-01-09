package io.github.johntortoise.core.manger.chat;

import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.ToolCall;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.dto.sys.LLmInvokeResp;
import io.github.johntortoise.core.dto.sys.ToolCallDTO;

import java.util.List;

public interface TortoiseChatManger {


    Boolean checkLimit(String conversationId);


    List<Message> findHistoryChat(String conversationId);


    void afterChat(AfterChatDTO afterChatDTO);

    String toolCallInvoke(ToolCallDTO toolCallDTO);

}
