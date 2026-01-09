package io.github.johntortoise.core.utils;

import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.ToolCall;
import io.github.johntortoise.core.dto.sys.LLmInvokeResp;
import io.github.johntortoise.core.dto.sys.ToolCallDTO;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.enums.RoleEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MessageUtil {
    public static List<Message> buildMessage(String prompt, String... array){
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(prompt, RoleEnum.SYSTEM.getCode()));
        for(String msg:array){
            messages.add(new Message(msg, RoleEnum.USER.getCode()));
        }
        return messages;
    }


    public static String tortoiseMsgForToolCallMsg(String req,String resp){
        return "工具调用:\n"+"    req:"+req+"\n"+"    resp"+resp+"\n";
    }

    public static List<ChatCompletionResponse.Message> toolCallInvoke(List<ToolCall> toolCalls, Function<ToolCallDTO, String> toolCallHandler){
        List<ChatCompletionResponse.Message> messages = new ArrayList<>();
        for(ToolCall toolCall:toolCalls){
            ChatCompletionResponse.Message message = new ChatCompletionResponse.Message();
            ToolCall.Function function = toolCall.getFunction();
            message.setRole(RoleEnum.TOOL.getCode());
            message.setToolCallId(toolCall.getId());
            try {
                message.setContent(toolCallHandler.apply(new ToolCallDTO(function.getArguments(),function.getName())));
            }catch (Exception e){
                message.setContent(e.getMessage());
            }
            messages.add(message);
        }
        return messages;
    }

    public static List<ChatCompletionResponse.Message> callTool(List<ToolCall> toolCalls, Function<ToolCallDTO, String> toolCallHandler, List<LLmInvokeResp.Event> list,
                                                                StreamCallBack streamCallBack){
        try {

            String req = ObjectMapperUtil.createObjectMapper().writeValueAsString(toolCalls);
            LogUtil.info(" [tortoise-llmCallToolReq]:{}",req);
            List<ChatCompletionResponse.Message> toolMessages = toolCallInvoke(toolCalls,toolCallHandler);
            String resp = ObjectMapperUtil.createObjectMapper().writeValueAsString(toolMessages);
            LogUtil.info(" [tortoise-llmCallToolResp]:{}",resp);

            list.add( new LLmInvokeResp.Event("tortoiseMsg",MessageUtil.tortoiseMsgForToolCallMsg(req,resp)));

            if(Objects.nonNull(streamCallBack) && EmptyUtil.isNotEmpty(MessageUtil.tortoiseMsgForToolCallMsg(req,resp))){
                streamCallBack.tortoiseMsg(MessageUtil.tortoiseMsgForToolCallMsg(req,resp));
            }
            return toolMessages;
        }catch (Exception e){
            LogUtil.error("rebuildWithToolCallInfo error",e);
            return new ArrayList<>();
        }
    }

    public static List<Message> createChatMessage(List<Message> history,Message input,List<Message> toolMessages){
        ArrayList<Message> messages = new ArrayList<>();
        if(EmptyUtil.isNotEmpty(history)){
            messages.addAll(history);
        }
        if(EmptyUtil.isNotEmpty(input)){
            messages.add(input);
        }
        if(EmptyUtil.isNotEmpty(toolMessages)){
            messages.addAll(toolMessages);
        }
        return messages;
    }

    public static void saveReasoningContent(List<LLmInvokeResp.Event> list,String reasoningContent){
        if(EmptyUtil.isNotEmpty(reasoningContent)){
            list.add(new LLmInvokeResp.Event("reasoningContent",reasoningContent));
        }
    }


}
