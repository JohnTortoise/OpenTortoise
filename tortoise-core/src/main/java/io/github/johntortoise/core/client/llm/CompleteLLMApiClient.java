package io.github.johntortoise.core.client.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.client.llm.helper.RequestBuilder;
import io.github.johntortoise.core.client.llm.helper.ResponseBuilder;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.sys.ConversationContext;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


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
                .build();    }

    public final String chatCompletion(List<Message> messages, String conversationId) throws IOException {
        Request request = buildChatRequest(messages, conversationId);
        try (Response response = client.newCall(request).execute()) {
            return handleChatResponse(response, conversationId);
        }
    }


    public static String invoke(ConversationContext context) {
        try {
            Model modelConfig = context.getModelConfig();
            CompleteLLMApiClient completeLLMApiClient = createClient(modelConfig);
            return completeLLMApiClient.chatCompletion(context.getMessages(), context.getConversationId());
        } catch (Exception e) {
            throw new TortoiseBusinessException(ErrorCodeEnum.INVOKE_LLM_ERROR, e.getMessage());
        }
    }


    public static CompleteLLMApiClient createClient(Model modelConfig) {
        return new CompleteLLMApiClient(modelConfig.getCompleteUrl(), modelConfig.getApiKey(), modelConfig.getModelName(),modelConfig.getTemperature());
    }

    protected Request buildChatRequest(List<Message> messages, String conversationId){
        return requestBuilder.buildChatRequest(messages, false, conversationId);
    }

    protected String handleChatResponse(Response response, String conversationId) {
        return responseBuilder.handleChatResponse(response, conversationId);
    }
}