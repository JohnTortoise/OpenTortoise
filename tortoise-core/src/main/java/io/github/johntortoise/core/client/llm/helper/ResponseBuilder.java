package io.github.johntortoise.core.client.llm.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.LogUtil;
import okhttp3.Response;

public class ResponseBuilder {
    private final ObjectMapper objectMapper;
    public ResponseBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String handleChatResponse(Response response, String conversationId)  {

        if (!response.isSuccessful()) {
            try {
                String responseBody = response.body().string();
                throw new RuntimeException(responseBody);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        try {
            String result = response.body().string();
            LogUtil.info("[tortoise-llmResp] conversationId={} | resp={}", conversationId, result);
            return result;
        }catch (Exception e){
            LogUtil.error("handleChatResponse error",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.UNKNOWN_ERROR,e.getMessage());
        }
    }


}