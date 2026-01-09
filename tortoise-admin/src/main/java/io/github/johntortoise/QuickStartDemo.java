package io.github.johntortoise;

import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.model.Tool;
import io.github.johntortoise.core.dto.sys.FieldDTO;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;
import io.github.johntortoise.core.utils.FunctionCallToolUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuickStartDemo {

    // 创建Tortoise客户端
    private final static TortoiseClient<ChatCompletionResponse> tortoiseClient =
        TortoiseClient.createForAdmin(ChatCompletionResponse.class,"http:localhost:8080","sk-428acddd-6ced-4e33-a9cb-ec96c980cf02");

    public static void main(String[] args) {

        String conversationId = tortoiseClient.createConversation("1112422");

        TortoiseMessage message = new TortoiseMessage("帮我查询一下聊天配置，每页10，关键词搜索摘要，第一页");
        message.setConversationId(conversationId);

        tortoiseClient.chatStreamForAdmin(message, new StreamCallBack() {
            @Override
            public void send(String content) {
                System.out.println("接收到推送消息"+content);
            }

            @Override
            public void finish() {

            }

            @Override
            public void onFailure() {

            }

            @Override
            public void tortoiseMsg(String content) {

            }
        },Arrays.asList("queryChatProfile"));


//        ChatCompletionResponse chatCompletionResponse = tortoiseClient.chatForAdmin(message, Arrays.asList("queryChatProfile"));
    }
}