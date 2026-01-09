package io.github.johntortoise.core.client.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.client.llm.helper.RequestBuilder;
import io.github.johntortoise.core.client.llm.helper.ResponseBuilder;
import io.github.johntortoise.core.dto.model.*;
import io.github.johntortoise.core.dto.sys.ConversationContext;
import io.github.johntortoise.core.dto.sys.LLmInvokeResp;
import io.github.johntortoise.core.dto.sys.ToolCallDTO;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.enums.RoleEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.core.utils.MessageUtil;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


public class CompleteLLMApiClient{
    protected final RequestBuilder requestBuilder;
    protected final ResponseBuilder responseBuilder;
    protected final OkHttpClient client;

    public CompleteLLMApiClient(String baseUrl, String apiKey, String modelName, BigDecimal temperature) {
        ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();
        if(Objects.isNull(temperature)){
            temperature = new BigDecimal("0.7");
        }
        this.requestBuilder = new RequestBuilder(baseUrl, apiKey, modelName, objectMapper,temperature);
        this.responseBuilder = new ResponseBuilder(objectMapper);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public final String chatCompletion(List<Message> messages, String conversationId) throws IOException {
        return chatCompletion(messages,conversationId,null);
    }

    public final String chatCompletion(List<Message> messages, String conversationId, List<Tool> tools) throws IOException {
        String body = buildBody(messages, tools);
        return chatCompletion(body,conversationId);
    }

    public final String chatCompletion(String body,String conversationId) throws IOException {
        Request request = buildChatRequest(body, conversationId);
        try (Response response = client.newCall(request).execute()) {
            return handleChatResponse(response, conversationId);
        }
    }


    public static LLmInvokeResp invokeBase(ConversationContext context, Function<ToolCallDTO, String> toolCallHandler){
        LLmInvokeResp lLmInvokeResp = new LLmInvokeResp();

        List<LLmInvokeResp.AllChat> allChatList = new ArrayList<>();
        List<LLmInvokeResp.Event> eventLists = new ArrayList<>();


        CompleteLLMApiClient completeLLMApiClient;
        Model modelConfig;

        try {
            modelConfig = context.getModelConfig();
            completeLLMApiClient = createClient(modelConfig);
            String currentResp;
            int toolDeep = 1;
            while (true) {
                if(toolDeep > context.getMaxToolDeep()){
                    throw new RuntimeException(String.format("超过调用工具最大次数%s",context.getMaxToolDeep()));
                }

                List<Message> messages = MessageUtil.createChatMessage(context.getHistoryMessage(), context.getInput(), context.getToolCallMessage());

                String req = completeLLMApiClient.buildBody(messages, context.getTools());

                currentResp = completeLLMApiClient.chatCompletion(
                        req,
                        context.getConversationId()
                );

                allChatList.add(new LLmInvokeResp.AllChat(req,currentResp));

                ChatCompletionResponse chatCompletionResponse = ObjectMapperUtil.createObjectMapper()
                        .readValue(currentResp, ChatCompletionResponse.class);
                ChatCompletionResponse.Message outPutmessage = chatCompletionResponse.getChoices()[0].getMessage();
                List<ToolCall> toolCalls = outPutmessage.getToolCalls();
                MessageUtil.saveReasoningContent(eventLists,outPutmessage.getReasoningContent());

                if (EmptyUtil.isNotEmpty(toolCalls)) {
                    List<ChatCompletionResponse.Message> functionCallResultMessageList = MessageUtil.callTool(toolCalls, toolCallHandler,eventLists,null);
                    context.getToolCallMessage().add(outPutmessage);
                    context.getToolCallMessage().addAll(functionCallResultMessageList);
                } else {
                    lLmInvokeResp.setLastResp(currentResp);
                    break;
                }
                toolDeep++;
            }
            lLmInvokeResp.setAllChatList(allChatList);
            return lLmInvokeResp;

        } catch (Exception e) {
            throw new TortoiseBusinessException(ErrorCodeEnum.INVOKE_LLM_ERROR, e.getMessage());
        }
    }




    public static CompleteLLMApiClient createClient(Model modelConfig) {
        return new CompleteLLMApiClient(modelConfig.getCompleteUrl(), modelConfig.getApiKey(), modelConfig.getModelName(),modelConfig.getTemperature());
    }


    protected Request buildChatRequest(String body,String conversationId){
        return requestBuilder.buildChatRequest(body,conversationId);
    }

    protected String buildBody(List<Message> messages,List<Tool> tools){
        return requestBuilder.generateBody(messages,false,tools);
    }

    protected String handleChatResponse(Response response, String conversationId) {
        return responseBuilder.handleChatResponse(response, conversationId);
    }
}