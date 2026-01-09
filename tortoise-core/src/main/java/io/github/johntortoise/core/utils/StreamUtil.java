package io.github.johntortoise.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.dto.model.ChatChunk;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.model.ToolCall;
import io.github.johntortoise.core.dto.model.Usage;

import java.util.List;
import java.util.Objects;

public class StreamUtil {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperUtil.createObjectMapper();

    private static class StreamDataAggregator {
        private String id = "";
        private String serviceTier = "";
        private Long created = 0L;
        private String model = "";
        private String role = "";
        private final StringBuilder reasoningContentBuilder = new StringBuilder();
        private String functionCallName = null;
        private final StringBuilder functionCallArgs = new StringBuilder();
        private String functionCallId = "";
        private String functionCallType = "";
        private final StringBuilder contentBuilder = new StringBuilder();
        private String finishReason = "";
        private Integer index = 0;
        private Usage usage = null;

        public void updateFromChunk(ChatChunk chatChunk) {
            if (chatChunk == null) {
                return;
            }

            // 更新基本信息
            updateBasicInfo(chatChunk);

            // 更新使用情况
            if (chatChunk.getUsage() != null) {
                this.usage = chatChunk.getUsage();
            }

            // 处理choices
            processChoices(chatChunk);
        }

        private void updateBasicInfo(ChatChunk chatChunk) {
            if (EmptyUtil.isNotEmpty(chatChunk.getModel())) {
                this.model = chatChunk.getModel();
            }
            if (EmptyUtil.isNotEmpty(chatChunk.getId())) {
                this.id = chatChunk.getId();
            }
            if (EmptyUtil.isNotEmpty(chatChunk.getServiceTier())) {
                this.serviceTier = chatChunk.getServiceTier();
            }
            if (EmptyUtil.isNotEmpty(chatChunk.getCreated())) {
                this.created = chatChunk.getCreated();
            }
        }

        private void processChoices(ChatChunk chatChunk) {
            if (EmptyUtil.isEmpty(chatChunk.getChoices())) {
                return;
            }

            ChatChunk.Choice choiceResp = chatChunk.getChoices().get(0);

            if (EmptyUtil.isNotEmpty(choiceResp.getIndex())) {
                this.index = choiceResp.getIndex();
            }
            if (EmptyUtil.isNotEmpty(choiceResp.getFinishReason())) {
                this.finishReason = choiceResp.getFinishReason();
            }

            ChatChunk.Delta delta = choiceResp.getDelta();
            if (delta == null) {
                return;
            }

            // 处理工具调用
            processToolCalls(delta);

            // 处理内容
            processContent(delta);

            // 处理角色
            if (EmptyUtil.isNotEmpty(delta.getRole())) {
                this.role = delta.getRole();
            }
        }

        private void processToolCalls(ChatChunk.Delta delta) {
            List<ToolCall> toolCalls = delta.getToolCalls();
            if (EmptyUtil.isEmpty(toolCalls)) {
                return;
            }

            for (ToolCall toolCall : toolCalls) {
                ToolCall.Function function = toolCall.getFunction();
                if (function != null && EmptyUtil.isNotEmpty(function.getName())) {
                    this.functionCallName = function.getName();
                }
                if (EmptyUtil.isNotEmpty(toolCall.getId())) {
                    this.functionCallId = toolCall.getId();
                }
                if (EmptyUtil.isNotEmpty(toolCall.getType())) {
                    this.functionCallType = toolCall.getType();
                }
                if (function != null && function.getArguments() != null) {
                    this.functionCallArgs.append(function.getArguments());
                }
            }
        }

        private void processContent(ChatChunk.Delta delta) {
            if (EmptyUtil.isNotEmpty(delta.getContent())) {
                this.contentBuilder.append(delta.getContent());
            }
            if (EmptyUtil.isNotEmpty(delta.getReasoningContent())) {
                this.reasoningContentBuilder.append(delta.getReasoningContent());
            }
        }

        public ChatCompletionResponse buildResponse() {
            ChatCompletionResponse response = new ChatCompletionResponse();
            ChatCompletionResponse.Choice choice = new ChatCompletionResponse.Choice();
            ChatCompletionResponse.Message message = buildMessage();

            // 设置choice
            choice.setMessage(message);
            choice.setReasoningContent(reasoningContentBuilder.toString());
            choice.setIndex(index);
            choice.setFinishReason(finishReason);

            // 设置response
            response.setModel(model);
            response.setUsage(usage);
            response.setCreated(created);
            response.setId(id);
            response.setServiceTier(serviceTier);
            response.setChoices(new ChatCompletionResponse.Choice[]{choice});

            return response;
        }

        private ChatCompletionResponse.Message buildMessage() {
            ChatCompletionResponse.Message message = new ChatCompletionResponse.Message();
            message.setRole(role);
            message.setContent(contentBuilder.toString());
            message.setReasoningContent(reasoningContentBuilder.toString());

            // 如果有工具调用，设置toolCalls
            if (EmptyUtil.isNotEmpty(functionCallName)) {
                ToolCall toolCall = buildToolCall();
                message.setToolCalls(java.util.Collections.singletonList(toolCall));
            }

            return message;
        }

        private ToolCall buildToolCall() {
            ToolCall toolCall = new ToolCall();
            ToolCall.Function function = new ToolCall.Function();
            function.setName(functionCallName);
            function.setArguments(functionCallArgs.toString());
            toolCall.setFunction(function);
            toolCall.setId(functionCallId);
            toolCall.setType(functionCallType);
            return toolCall;
        }
    }


    public static String processStreamResponse(List<String> resp)
            throws JsonProcessingException {

        // 参数校验
        if (EmptyUtil.isEmpty(resp)) {
            throw new IllegalArgumentException("响应列表不能为空");
        }

        StreamDataAggregator aggregator = new StreamDataAggregator();

        try {
            for (String jsonStr : resp) {
                if (EmptyUtil.isEmpty(jsonStr)) {
                    continue; // 跳过空字符串
                }

                ChatChunk chatChunk = OBJECT_MAPPER.readValue(jsonStr, ChatChunk.class);
                aggregator.updateFromChunk(chatChunk);
            }

            ChatCompletionResponse response = aggregator.buildResponse();
            return OBJECT_MAPPER.writeValueAsString(response);

        } catch (JsonProcessingException e) {
            // 重新抛出JSON处理异常
            throw e;
        } catch (Exception e) {
            // 包装其他异常
            throw new RuntimeException("处理流式响应时发生错误", e);
        }
    }


}