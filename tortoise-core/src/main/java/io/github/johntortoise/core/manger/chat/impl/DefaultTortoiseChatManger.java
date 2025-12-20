package io.github.johntortoise.core.manger.chat.impl;

import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.manger.chat.TortoiseChatManger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTortoiseChatManger implements TortoiseChatManger {
    private static final ConcurrentHashMap<String,List<Message>> map = new ConcurrentHashMap<>();

    @Override
    public List<Message> findHistoryChat(String conversationId) {
        List<Message> messages = map.computeIfAbsent(conversationId, key -> new ArrayList<>());
        return new ArrayList<>(messages);
    }

    @Override
    public void afterChat(AfterChatDTO afterChatDTO) {
        MainInfo mainInfo = afterChatDTO.getMainInfo();
        List<Message> history = mainInfo.getHistory();
        Message input = mainInfo.getInput();
        Message outPut = mainInfo.getOutPut();

        history.add(input);
        history.add(outPut);
        map.put(afterChatDTO.getConversationId(),history);
    }

    @Override
    public Boolean checkLimit(String conversationId){
        return false;
    }
}
