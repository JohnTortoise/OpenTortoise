package io.github.johntortoise.core.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.admin.TortoiseAdminClient;
import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.client.llm.CompleteLLMApiClient;
import io.github.johntortoise.core.client.llm.StreamLLMApiClient;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.dto.sys.ConversationContext;
import io.github.johntortoise.core.dto.sys.ReceiveMessageReq;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.manger.chat.TortoiseChatManger;
import io.github.johntortoise.core.manger.chat.impl.AdminTortoiseChatManger;
import io.github.johntortoise.core.manger.chat.impl.DefaultTortoiseChatManger;
import io.github.johntortoise.core.message.TortoiseMessageHandler;
import io.github.johntortoise.core.message.impl.DefaultTortoiseMessageHandler;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import io.github.johntortoise.core.valid.BeforeChatValid;
import io.github.johntortoise.core.valid.chain.ValidationChain;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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



    public T chat(TortoiseMessage tortoiseMessage, Model model){
        if(tortoiseChatManger.checkLimit(tortoiseMessage.getConversationId())){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"超过token上限");
        }
        String conversationId = tortoiseMessage.getConversationId();

        performPreValidation(tortoiseMessage,model);

        List<Message> historyMemory = tortoiseChatManger.findHistoryChat(conversationId);

        Message input = new Message(tortoiseMessage.getMessage().getContent(), tortoiseMessage.getMessage().getRole());
        ConversationContext context = buildConversationContext(historyMemory,input,conversationId, model);

        String llmResp =  CompleteLLMApiClient.invoke(context);

        tortoiseChatManger.afterChat(
                AfterChatDTO.builder()
                        .mainInfo(this.tortoiseMessageHandler.convertForMainInfo(historyMemory,input,llmResp,null))
                        .conversationId(conversationId)
                        .build());

        return this.tortoiseMessageHandler.convertForResult(llmResp);

    }


    public T chatForAdmin(TortoiseMessage tortoiseMessage){
        Model model = tortoiseAdminClient.findModel(tortoiseMessage.getConversationId());
        return chat(tortoiseMessage,model);
    }


    public void chatStream(TortoiseMessage tortoiseMessage, Model model, StreamCallBack streamCallBack){
        if(tortoiseChatManger.checkLimit(tortoiseMessage.getConversationId())){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"超过上限");
        }
        String conversationId = tortoiseMessage.getConversationId();

        performPreValidation(tortoiseMessage,model);

        List<Message> historyMemory = tortoiseChatManger.findHistoryChat(conversationId);

        Message input = new Message(tortoiseMessage.getMessage().getContent(), tortoiseMessage.getMessage().getRole());

        ConversationContext context = buildConversationContext(historyMemory,input,conversationId, model);
        StreamLLMApiClient.invoke(context,streamCallBack, new StreamCallBack() {
            final List<String> result = new ArrayList<>();
            @Override
            public void send(String content) {
                result.add(content);
            }

            @Override
            public void finish(){
                try {
                    ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();
                    String llmResp = objectMapper.writeValueAsString(result);
                    tortoiseChatManger.afterChat(
                            AfterChatDTO.builder()
                                    .mainInfo(tortoiseMessageHandler.convertForMainInfo(historyMemory,input,llmResp, streamCallBack))
                                    .conversationId(conversationId)
                                    .build());
                }catch (Exception e){
                    LogUtil.error("格式化失败",e);
                }
            }
            @Override
            public void onFailure() {

            }
        });
    }



    public void chatStreamForAdmin(TortoiseMessage tortoiseMessage, StreamCallBack streamCallBack){
        Model model = tortoiseAdminClient.findModel(tortoiseMessage.getConversationId());
        chatStream(tortoiseMessage,model,streamCallBack);
    }


    private ConversationContext buildConversationContext(List<Message> history, Message input,
                                                         String conversationId,
                                                         Model model) {
        ArrayList<Message> messages = new ArrayList<>(history);
        messages.add(input);

        return ConversationContext.builder()
                .messages(messages)
                .modelConfig(model)
                .conversationId(conversationId)
                .timestamp(System.currentTimeMillis())
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
