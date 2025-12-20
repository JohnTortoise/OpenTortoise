package io.github.johntortoise.core.message.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.dto.model.ChatChunk;
import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Usage;
import io.github.johntortoise.core.dto.sys.ProcessedApiResult;
import io.github.johntortoise.core.dto.sys.ProcessedStreamResult;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.message.TortoiseMessageHandler;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;

import java.util.Collections;
import java.util.List;

public class DefaultTortoiseMessageHandler<T> implements TortoiseMessageHandler<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> responseClass;

    public DefaultTortoiseMessageHandler(Class<T> responseClass) {
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.responseClass = responseClass;
    }

    @Override
    public T convertForResult(String resp){
        try {

            if (responseClass == String.class) {
                return (T) resp;
            }
            return objectMapper.readValue(resp, responseClass);
        }catch (Exception e){
            throw new TortoiseBusinessException(ErrorCodeEnum.PARSE_RESULT_ERROR,"JSON parsing error");
        }
    }



    @Override
    public MainInfo convertForMainInfo(List<Message> history, Message input, String resp, StreamCallBack streamCallBack) {
        try {


            final MainInfo.Tokens tokens = new MainInfo.Tokens();
            final String content;
            final String role;

            if (streamCallBack != null) {

                ProcessedStreamResult streamResult = processStreamResponse(resp);
                content = streamResult.getContent();
                role = streamResult.getRole();
                updateTokensFromUsage(streamResult.getUsage(), tokens);
            } else {

                ProcessedApiResult apiResult = processApiResponse(resp);
                content = apiResult.getContent();
                role = apiResult.getRole();
                updateTokensFromUsage(apiResult.getUsage(), tokens);
            }
            return MainInfo.builder()
                    .history(history != null ? history : Collections.emptyList())
                    .input(input)
                    .outPut(new Message(content, role))
                    .tokens(tokens)
                    .build();
        } catch (JsonProcessingException e) {
            LogUtil.error("JSON解析失败，响应内容格式错误", e);
            throw new TortoiseBusinessException(ErrorCodeEnum.PARSE_RESULT_ERROR, "Invalid response format");
        } catch (IllegalArgumentException e) {
            LogUtil.error("参数验证失败", e);
            throw new TortoiseBusinessException(ErrorCodeEnum.PARSE_RESULT_ERROR, e.getMessage());
        } catch (Exception e) {
            LogUtil.error("提取主信息失败", e);
            throw new TortoiseBusinessException(ErrorCodeEnum.PARSE_RESULT_ERROR, "Error in extracting brief information");
        }
    }


    private ProcessedStreamResult processStreamResponse(String resp) throws JsonProcessingException {
        StringBuilder contentBuilder = new StringBuilder();
        String role = "";
        Usage usage = null;

        List<String> jsonStrings = objectMapper.readValue(resp, new TypeReference<List<String>>() {});

        for (String jsonStr : jsonStrings) {
            ChatChunk chatChunk = objectMapper.readValue(jsonStr, ChatChunk.class);


            if (EmptyUtil.isNotEmpty(chatChunk.getChoices())) {
                ChatChunk.Delta delta = chatChunk.getChoices().get(0).getDelta();
                if (delta != null) {
                    if (EmptyUtil.isNotEmpty(delta.getContent())) {
                        contentBuilder.append(delta.getContent());
                    }
                    if (EmptyUtil.isNotEmpty(delta.getRole())) {
                        role = delta.getRole();
                    }
                }
            }


            if (chatChunk.getUsage() != null) {
                usage = chatChunk.getUsage();
            }
        }

        return new ProcessedStreamResult(contentBuilder.toString(), role, usage);
    }


    private ProcessedApiResult processApiResponse(String resp) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(resp);


        JsonNode choicesNode = rootNode.path("choices");
        if (choicesNode.isMissingNode() || !choicesNode.isArray() || choicesNode.isEmpty()) {
            throw new IllegalArgumentException("Response missing valid choices field");
        }

        JsonNode firstChoice = choicesNode.get(0);
        if (firstChoice == null) {
            throw new IllegalArgumentException("No choices available in response");
        }

        JsonNode messageNode = firstChoice.path("message");
        String content = messageNode.path("content").asText("");
        String role = messageNode.path("role").asText("");


        JsonNode usageNode = rootNode.path("usage");
        Usage usage = null;
        if (!usageNode.isMissingNode()) {
            usage = objectMapper.readValue(usageNode.toString(), Usage.class);
        }

        return new ProcessedApiResult(content, role, usage);
    }


    private void updateTokensFromUsage(Usage usage, MainInfo.Tokens tokens) {
        if (usage == null) {
            return;
        }
        tokens.setCachedTokens(getCachedTokensSafely(usage));
        tokens.setCompletionTokens(usage.getCompletionTokens());
        tokens.setPromptTokens(usage.getPromptTokens());
        tokens.setTotalTokens(usage.getTotalTokens());
    }


    private int getCachedTokensSafely(Usage usage) {
        try {
            Usage.PromptTokensDetails details = usage.getPromptTokensDetails();
            return details != null ? details.getCachedTokens() : 0;
        } catch (Exception e) {
            LogUtil.error("Failed to get cached tokens, using default 0", e);
            return 0;
        }
    }

}