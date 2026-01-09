package io.github.johntortoise.core.manger.chat.impl;

import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.ToolCall;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.dto.sys.LLmInvokeResp;
import io.github.johntortoise.core.dto.sys.ToolCallDTO;
import io.github.johntortoise.core.manger.chat.TortoiseChatManger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public String toolCallInvoke(ToolCallDTO toolCallDTO) {
        if(toolCallDTO.getName().equals("queryDate")){
            return LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if(toolCallDTO.getName().equals("queryWeather")){
            String arguments = toolCallDTO.getArguments();
            System.out.println(arguments);
            if(true){
                throw new RuntimeException("出错了");
            }
            return "阴天";
        }
        return "";
    }


    @Override
    public Boolean checkLimit(String conversationId){
        return false;
    }
}
