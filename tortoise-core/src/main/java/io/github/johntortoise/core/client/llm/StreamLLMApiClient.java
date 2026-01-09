package io.github.johntortoise.core.client.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.client.llm.helper.RequestBuilder;
import io.github.johntortoise.core.client.llm.helper.ResponseBuilder;
import io.github.johntortoise.core.dto.model.*;
import io.github.johntortoise.core.dto.sys.ConversationContext;
import io.github.johntortoise.core.dto.sys.LLmInvokeResp;
import io.github.johntortoise.core.dto.sys.ToolCallDTO;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.enums.RoleEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.*;
import okhttp3.*;
import okio.BufferedSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


public class StreamLLMApiClient {
    protected final RequestBuilder requestBuilder;
    protected final ResponseBuilder responseBuilder;
    protected final OkHttpClient client;

    public StreamLLMApiClient(String baseUrl, String apiKey, String modelName, BigDecimal temperature) {
        ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();
        this.requestBuilder = new RequestBuilder(baseUrl, apiKey, modelName, objectMapper,temperature);
        this.responseBuilder = new ResponseBuilder(objectMapper);

        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();    }


    protected Request buildChatRequest(List<Message> messages, String conversationId,List<Tool> tools) {
        String body = requestBuilder.generateBody(messages, true, tools);
        return requestBuilder.buildChatRequest(body, conversationId);
    }

    protected Request buildChatRequest(String body,String conversationId){
        return requestBuilder.buildChatRequest(body,conversationId);
    }

    protected String buildBody(List<Message> messages,List<Tool> tools){
        return requestBuilder.generateBody(messages,true,tools);
    }


    public static LLmInvokeResp invokeBase(ConversationContext context, StreamCallBack sysCallBack, Function<ToolCallDTO, String> toolCallHandler
                                           ) {
        LLmInvokeResp lLmInvokeResp = new LLmInvokeResp();

        List<LLmInvokeResp.AllChat> allChatList = new ArrayList<>();
        List<LLmInvokeResp.Event> eventLists = new ArrayList<>();

        StreamLLMApiClient streamLLMApiClient;
        Model modelConfig;

        try {
            modelConfig = context.getModelConfig();
            streamLLMApiClient = createClient(modelConfig);
            String currentResp;

            int toolDeep = 1;

            while (true) {
                if(toolDeep > context.getMaxToolDeep()){
                    throw new RuntimeException(String.format("超过调用工具最大次数%s",context.getMaxToolDeep()));
                }

                List<Message> messages = MessageUtil.createChatMessage(context.getHistoryMessage(), context.getInput(), context.getToolCallMessage());

                String req = streamLLMApiClient.buildBody(messages, context.getTools());

                currentResp = streamLLMApiClient.chatCompletion(
                        req,
                        sysCallBack,
                        context.getConversationId()
                );
                allChatList.add(new LLmInvokeResp.AllChat(req,currentResp));

                ChatCompletionResponse chatCompletionResponse = ObjectMapperUtil.createObjectMapper()
                        .readValue(currentResp, ChatCompletionResponse.class);
                ChatCompletionResponse.Message outPutmessage = chatCompletionResponse.getChoices()[0].getMessage();
                List<ToolCall> toolCalls = outPutmessage.getToolCalls();

                MessageUtil.saveReasoningContent(eventLists,outPutmessage.getReasoningContent());

                if (EmptyUtil.isNotEmpty(toolCalls)) {
                    eventLists.add(new LLmInvokeResp.Event("message",outPutmessage.getContent()));
                    List<ChatCompletionResponse.Message> functionCallResultMessageList = MessageUtil.callTool(toolCalls, toolCallHandler,eventLists,sysCallBack);
                    context.getToolCallMessage().add(outPutmessage);
                    context.getToolCallMessage().addAll(functionCallResultMessageList);
                } else {
                    lLmInvokeResp.setLastResp(currentResp);
                    break;
                }
                toolDeep++;
            }


            lLmInvokeResp.setAllChatList(allChatList);
            lLmInvokeResp.setEventLists(eventLists);
            return lLmInvokeResp;

        } catch (Exception e) {
            throw new TortoiseBusinessException(ErrorCodeEnum.INVOKE_LLM_ERROR, e.getMessage());
        }
    }



    public static StreamLLMApiClient createClient(Model modelConfig){
        return new StreamLLMApiClient(
                modelConfig.getCompleteUrl(),
                modelConfig.getApiKey(),
                modelConfig.getModelName(),
                modelConfig.getTemperature()
        );
    }



    public final String chatCompletion(String body,StreamCallBack sysCallBack,String conversationId){
        Request request = buildChatRequest(body, conversationId);

        List<String> streamDataList = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(1);
        try {

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if(Objects.nonNull(sysCallBack)){
                        sysCallBack.onFailure();
                    }
                }
                @Override
                public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful()) {
                        if(Objects.nonNull(sysCallBack)){
                            sysCallBack.onFailure();
                        }
                        return;
                    }

                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody != null) {
                            BufferedSource source = responseBody.source();

                            while (!source.exhausted()) {
                                String line = source.readUtf8Line();
                                if (line != null) {
                                    if (line.trim().isEmpty() || line.startsWith("event:")) {
                                        continue;
                                    }
                                    if (line.startsWith("data: ")) {
                                        String data = line.substring(6);

                                        if ("[DONE]".equals(data.trim())) {
                                            if(Objects.nonNull(sysCallBack)){
                                                latch.countDown();
                                            }
                                            break;
                                        }

                                        if(Objects.nonNull(sysCallBack)){
                                            sysCallBack.send(data);
                                            streamDataList.add(data);
                                        }

                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        if(Objects.nonNull(sysCallBack)){
                            sysCallBack.onFailure();
                        }
                    }
                }
            });
            latch.await();
            return StreamUtil.processStreamResponse(streamDataList);
        } catch (Exception e) {
            throw new TortoiseBusinessException(ErrorCodeEnum.INVOKE_LLM_ERROR, e.getMessage());
        }
    }

    public final String chatCompletion(List<Message> messages, String conversationId,
                                     StreamCallBack sysCallBack, List<Tool> tools) throws IOException {
        String body = buildBody(messages, tools);
        return chatCompletion(body,sysCallBack,conversationId);
    }


    public final void chatCompletion(List<Message> messages, String conversationId,
                                     StreamCallBack sysCallBack) throws IOException {
        chatCompletion(messages,conversationId,sysCallBack,null);
    }
}