package io.github.johntortoise.core.client.llm.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.consts.HTTPConst;
import io.github.johntortoise.core.dto.model.ChatCompletionRequest;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.LogUtil;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.math.BigDecimal;
import java.util.List;

public class RequestBuilder {
    private final String completeUrl;
    private final String apiKey;
    private final String modelName;
    private final BigDecimal temperature;
    private final ObjectMapper objectMapper;

    public RequestBuilder(String completeUrl, String apiKey, String modelName, ObjectMapper objectMapper,BigDecimal temperature) {
        this.completeUrl = completeUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.objectMapper = objectMapper;
        this.temperature = temperature;
    }

    public Request buildChatRequest(List<Message> messages, boolean stream, String conversationId) {
        try {

            ChatCompletionRequest requestBody = new ChatCompletionRequest(
                    modelName, messages, this.temperature, stream, stream
            );

            String messagesStr = objectMapper.writeValueAsString(requestBody);

            Request request= new Request.Builder()
                        .url(completeUrl)
                        .post(RequestBody.create(messagesStr, MediaType.parse(HTTPConst.APPLICATION_JSON)))
                        .header(HTTPConst.CONTENT_TYPE, HTTPConst.APPLICATION_JSON)
                        .header(HTTPConst.AUTHORIZATION, HTTPConst.BEARER + apiKey)
                        .build();

            LogUtil.info("[tortoise-llmReq] conversationId={} | Url={} | Body={}",
                    conversationId, completeUrl, messagesStr);

            return request;
        }catch (Exception e){
            LogUtil.error("buildChatRequest error",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.UNKNOWN_ERROR,e.getMessage());
        }
    }
}