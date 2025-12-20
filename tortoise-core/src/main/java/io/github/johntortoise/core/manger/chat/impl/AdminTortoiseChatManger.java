
package io.github.johntortoise.core.manger.chat.impl;

import io.github.johntortoise.admin.TortoiseAdminClient;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.manger.chat.TortoiseChatManger;

import java.util.List;

public class AdminTortoiseChatManger implements TortoiseChatManger {

    private final TortoiseAdminClient tortoiseAdminClient;

    public AdminTortoiseChatManger(TortoiseAdminClient tortoiseAdminClient){
        this.tortoiseAdminClient = tortoiseAdminClient;
    }

    @Override
    public List<Message> findHistoryChat(String conversationId) {
        return tortoiseAdminClient.getMemoriesByConversationId(conversationId);
    }

    @Override
    public void afterChat(AfterChatDTO afterChatDTO) {
        tortoiseAdminClient.afterChat(afterChatDTO);
    }

    @Override
    public Boolean checkLimit(String conversationId) {
        return tortoiseAdminClient.checkLimit(conversationId);
    }
}
