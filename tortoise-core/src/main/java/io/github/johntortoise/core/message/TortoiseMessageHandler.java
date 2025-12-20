package io.github.johntortoise.core.message;

import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.core.dto.model.Message;

import java.util.List;

public interface TortoiseMessageHandler<T> {

    T convertForResult(String resp);


    MainInfo convertForMainInfo(List<Message> history, Message input, String resp, StreamCallBack streamCallBack);
}