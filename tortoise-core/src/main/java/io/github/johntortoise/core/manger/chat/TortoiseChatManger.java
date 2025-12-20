package io.github.johntortoise.core.manger.chat;

import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;

import java.util.List;

public interface TortoiseChatManger {


    Boolean checkLimit(String conversationId);


    List<Message> findHistoryChat(String conversationId);


    void afterChat(AfterChatDTO afterChatDTO);

}
