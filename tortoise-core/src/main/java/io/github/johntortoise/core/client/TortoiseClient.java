package io.github.johntortoise.core.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.admin.TortoiseAdminClient;
import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.client.llm.CompleteLLMApiClient;
import io.github.johntortoise.core.client.llm.StreamLLMApiClient;
import io.github.johntortoise.core.dto.model.*;
import io.github.johntortoise.core.dto.sys.*;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.manger.chat.TortoiseChatManger;
import io.github.johntortoise.core.manger.chat.impl.AdminTortoiseChatManger;
import io.github.johntortoise.core.manger.chat.impl.DefaultTortoiseChatManger;
import io.github.johntortoise.core.message.TortoiseMessageHandler;
import io.github.johntortoise.core.message.impl.DefaultTortoiseMessageHandler;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import io.github.johntortoise.core.utils.StreamUtil;
import io.github.johntortoise.core.valid.BeforeChatValid;
import io.github.johntortoise.core.valid.chain.ValidationChain;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Data
public class TortoiseClient<T>  {

    private final TortoiseChatManger tortoiseChatManger;
    private final TortoiseMessageHandler<T> tortoiseMessageHandler;
    private final Model model;
    private final TortoiseAdminClient tortoiseAdminClient;

    public TortoiseClient(TortoiseChatManger tortoiseChatManger,
                          TortoiseMessageHandler<T> tortoiseMessageHandler,
                          Model model,
                          TortoiseAdminClient tortoiseAdminClient) {
        this.tortoiseChatManger = tortoiseChatManger;
        this.tortoiseMessageHandler = tortoiseMessageHandler;
        this.model = model;
        this.tortoiseAdminClient = tortoiseAdminClient;
    }



    public static <T> TortoiseClient<T> create(Class<T> responseType) {
        return new TortoiseClient<>(
                new DefaultTortoiseChatManger(),
                new DefaultTortoiseMessageHandler<>(responseType),
                null,null);
    }

    public static <T> TortoiseClient<T> createForAdmin(Class<T> responseType,String host,String sk) {
        TortoiseAdminClient tortoiseAdminClient = new TortoiseAdminClient(host,sk);
        checkConnection(tortoiseAdminClient);
        return new TortoiseClient<>(
                new AdminTortoiseChatManger(tortoiseAdminClient),
                new DefaultTortoiseMessageHandler<>(responseType),
                null,
                tortoiseAdminClient);
    }

    public static void checkConnection(TortoiseAdminClient tortoiseAdminClient){
        try {
            tortoiseAdminClient.connect();
        }catch (Exception e){
            LogUtil.error("checkConnection error",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.INIT_CLIENT_ERROR,"连接admin异常"+e.getMessage());
        }
    }



    public T chat(TortoiseMessage tortoiseMessage, Model model) {
        return chat(tortoiseMessage, model, null);
    }

    public T chat(TortoiseMessage tortoiseMessage, Model model, List<Tool> tools) {
        Message input = new Message(tortoiseMessage.getMessage().getContent(), tortoiseMessage.getMessage().getRole());
        String conversationId = tortoiseMessage.getConversationId();
        List<Message> historyMemory = tortoiseChatManger.findHistoryChat(conversationId);

        beforeCall(tortoiseMessage, model);

        ConversationContext context = buildConversationContext(historyMemory, input, conversationId, model, tools);

        LLmInvokeResp lLmInvokeResp = CompleteLLMApiClient.invokeBase(context,tortoiseChatManger::toolCallInvoke);

        String llmResp = lLmInvokeResp.getLastResp();

        tortoiseChatManger.afterChat(
                AfterChatDTO.builder()
                        .mainInfo(this.tortoiseMessageHandler.convertForMainInfo(historyMemory, input, llmResp, null))
                        .allChatInfo(lLmInvokeResp.getAllChatList())
                        .toolCallMessages(context.getToolCallMessage())
                        .events(lLmInvokeResp.getEventLists())
                        .conversationId(conversationId)
                        .build());

        return this.tortoiseMessageHandler.convertForResult(llmResp);
    }



    public void  beforeCall(TortoiseMessage tortoiseMessage,Model model){
        if(tortoiseChatManger.checkLimit(tortoiseMessage.getConversationId())){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"超过token上限");
        }
        performPreValidation(tortoiseMessage,model);
    }

    public T chatForAdmin(TortoiseMessage tortoiseMessage){
        Model model = tortoiseAdminClient.findModel(tortoiseMessage.getConversationId());
        return chat(tortoiseMessage,model);
    }

    public T chatForAdmin(TortoiseMessage tortoiseMessage,List<String> toolNames){
        Model model = tortoiseAdminClient.findModel(tortoiseMessage.getConversationId());
        List<Tool> tools = tortoiseAdminClient.batchGetToolByNames(toolNames);
        return chat(tortoiseMessage,model,tools);
    }

    public String chatStream(TortoiseMessage tortoiseMessage, Model model, StreamCallBack streamCallBack,List<Tool> tools){
        if(tortoiseChatManger.checkLimit(tortoiseMessage.getConversationId())){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"超过上限");
        }
        String conversationId = tortoiseMessage.getConversationId();

        performPreValidation(tortoiseMessage,model);

        List<Message> historyMemory = tortoiseChatManger.findHistoryChat(conversationId);

        Message input = new Message(tortoiseMessage.getMessage().getContent(), tortoiseMessage.getMessage().getRole());

        ConversationContext context = buildConversationContext(historyMemory,input,conversationId, model,tools);

        LLmInvokeResp lLmInvokeResp = StreamLLMApiClient.invokeBase(context, streamCallBack,tortoiseChatManger::toolCallInvoke);


        String llmResp = lLmInvokeResp.getLastResp();

        tortoiseChatManger.afterChat(
                AfterChatDTO.builder()
                        .mainInfo(this.tortoiseMessageHandler.convertForMainInfo(historyMemory, input, llmResp, null))
                        .allChatInfo(lLmInvokeResp.getAllChatList())
                        .toolCallMessages(context.getToolCallMessage())
                        .events(lLmInvokeResp.getEventLists())
                        .conversationId(conversationId)
                        .build());

        streamCallBack.finish();

        return llmResp;
    }


    public String chatStream(TortoiseMessage tortoiseMessage, Model model, StreamCallBack streamCallBack){
        return chatStream(tortoiseMessage,model,streamCallBack,null);
    }



    public String chatStreamForAdmin(TortoiseMessage tortoiseMessage, StreamCallBack streamCallBack){
        Model model = tortoiseAdminClient.findModel(tortoiseMessage.getConversationId());
        return chatStream(tortoiseMessage,model,streamCallBack);
    }

    public void chatStreamForAdmin(TortoiseMessage tortoiseMessage, StreamCallBack streamCallBack,List<String> toolNames){
        Model model = tortoiseAdminClient.findModel(tortoiseMessage.getConversationId());
        List<Tool> tools = tortoiseAdminClient.batchGetToolByNames(toolNames);
        chatStream(tortoiseMessage,model,streamCallBack,tools);
    }


    private ConversationContext buildConversationContext(List<Message> history, Message input,
                                                         String conversationId,
                                                         Model model,List<Tool> tools) {
        return ConversationContext.builder()
                .historyMessage(history)
                .input(input)
                .toolCallMessage(new ArrayList<>())
                .modelConfig(model)
                .conversationId(conversationId)
                .timestamp(System.currentTimeMillis())
                .tools(tools)
                .build();
    }


    private void performPreValidation(TortoiseMessage tortoiseMessage, Model model) {
        ValidationChain.create()
                .addCheck(()-> BeforeChatValid.checkInit(model,this))
                .addCheck(() -> BeforeChatValid.validate(tortoiseMessage))
                .execute();
    }

    public String createConversation(String customerId){
        return tortoiseAdminClient.createConversation(customerId);
    }

    public void sendMessage(ReceiveMessageReq req){
        tortoiseAdminClient.receiveMessage(req);
    }

    public List<Message> getMemory(String conversationId){
        return tortoiseAdminClient.getMemoriesByConversationId(conversationId);
    }
}
