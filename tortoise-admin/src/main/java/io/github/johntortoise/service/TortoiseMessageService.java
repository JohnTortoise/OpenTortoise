package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.model.TortoiseMessage;

import java.util.List;
import java.util.Set;


public interface TortoiseMessageService extends IService<TortoiseMessage> {
    
    
    Page<Message> page(String conversationId, Long current, Long size);

    void writeMessagesAndRecordUsage(String conversationId, Message input, Message outPut, AfterChatDTO afterChatDTO);

    
    List<TortoiseMessage> listRecentByConversationId(String conversationId, Long limit);

    
    List<TortoiseMessage> listByConversationIdAndGreaterThanId(String conversationId, Long lastMessageId);

    
    TortoiseMessage findGtLastMessageId(String conversationId, Long lastMessage, Boolean asc);

    List<TortoiseMessage> findByUniqueIdList(Set<String> uniqueIdSet);

}
