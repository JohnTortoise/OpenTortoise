package io.github.johntortoise.core.utils;

import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.enums.RoleEnum;

import java.util.ArrayList;
import java.util.List;

public class MessageUtil {
    public static List<Message> buildMessage(String prompt, String... array){
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(prompt, RoleEnum.SYSTEM.getCode()));
        for(String msg:array){
            messages.add(new Message(msg, RoleEnum.USER.getCode()));
        }
        return messages;
    }
}
