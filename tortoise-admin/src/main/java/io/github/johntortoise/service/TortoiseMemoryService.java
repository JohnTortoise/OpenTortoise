package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.model.TortoiseMemory;

import java.util.List;


public interface TortoiseMemoryService extends IService<TortoiseMemory> {

    
    List<Message> getMemoriesByConversationId(String conversationId);


    
    TortoiseMemory findMemoryByConversation(String conversationId);

}