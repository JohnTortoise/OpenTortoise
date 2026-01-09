package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.client.TortoiseClient;
import io.github.johntortoise.core.dto.model.ChatChunk;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
import io.github.johntortoise.core.dto.sys.TortoiseMessage;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.context.TortoiseContext;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import io.github.johntortoise.dto.SendMessageReq;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.model.TortoiseChatProfile;
import io.github.johntortoise.model.TortoiseConversation;
import io.github.johntortoise.service.TortoiseChatProfileService;
import io.github.johntortoise.service.TortoiseConversationService;
import io.github.johntortoise.service.TortoiseMessageService;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.shadow.org.terracotta.offheapstore.concurrent.ConcurrentOffHeapHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@Slf4j
@RequestMapping("/api/chat")
public class TortoiseChatController {

    @Resource
    private TortoiseConversationService conversationService;

    @Resource
    private TortoiseMessageService messageService;

    @Resource
    private TortoiseChatProfileService chatProfileService;

    @Value("${server.port:8080}")
    private Integer serverPort;


    /**
     * 创建新会话
     */
    @PostMapping("/conversation/create")
    public TortoiseBaseResult<TortoiseConversation> createConversation(
            @RequestParam @NotNull(message = "聊天配置ID不能为空") Long chatProfileId) {

        try {
            LogUtil.info("创建新会话: chatProfileId={}", chatProfileId);

            Long userId = TortoiseContext.getCurrentUserId();
            TortoiseConversation conversation = conversationService.createConversation(userId, chatProfileId);

            LogUtil.info("创建新会话成功: conversationId={}", conversation.getConversationId());
            return TortoiseBaseResult.ok(conversation);

        } catch (Exception e) {
            LogUtil.error("创建新会话失败: chatProfileId={}", chatProfileId, e);
            throw e;
        }
    }


    /**
     * 发送聊天消息（SSE流式输出）
     */
    @PostMapping(value = "/send")
    public SseEmitter sendMessage(@RequestBody SendMessageReq sendMessageReq) {

        List<String> toolNames = sendMessageReq.getToolNames();
        String conversationId = sendMessageReq.getConversationId();
        String content = sendMessageReq.getContent();

        // 创建SSE发射器，设置5分钟超时
        SseEmitter emitter = new SseEmitter(300000L);
        emitter.onCompletion(() -> LogUtil.info("SSE连接完成: conversationId={}", conversationId));
        emitter.onTimeout(() -> {
            LogUtil.warn("SSE连接超时: conversationId={}", conversationId);
            emitter.complete();
        });
        emitter.onError((throwable) -> {
            LogUtil.error("SSE连接错误: conversationId={}", conversationId, throwable);
        });

        // 异步处理流式响应
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                LogUtil.info("开始处理SSE流式聊天消息: conversationId={}, contentLength={}",
                        conversationId, content.length());

                // 获取会话信息
                TortoiseConversationDTO conversationDTO = conversationService.getByConversationId(conversationId);
                if (conversationDTO == null) {
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "会话不存在");
                }

                Long tortoiseChatProfileId = conversationDTO.getTortoiseChatProfileId();
                TortoiseChatProfile tortoiseChatProfile = chatProfileService.getById(tortoiseChatProfileId);
                if (tortoiseChatProfile == null) {
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "聊天配置不存在");
                }

                // 构建AI客户端
                TortoiseClient<ChatCompletionResponse> tortoiseClient = TortoiseClient.createForAdmin(
                        ChatCompletionResponse.class,
                        "http://localhost:" + serverPort,
                        tortoiseChatProfile.getSk()
                );

                TortoiseMessage message = new TortoiseMessage(content);
                message.setConversationId(conversationId);

                ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();

                AtomicBoolean completed = new java.util.concurrent.atomic.AtomicBoolean(false);


                // 流式调用AI服务
                tortoiseClient.chatStreamForAdmin(message, new StreamCallBack() {
                    @Override
                    public void send(String content) {
                        try {
                            if (completed.get()) {
                                return;
                            }
                            if (content != null && !content.trim().isEmpty()) {
                                try {
                                    io.github.johntortoise.core.dto.model.ChatChunk chatChunk =
                                        objectMapper.readValue(content, io.github.johntortoise.core.dto.model.ChatChunk.class);

                                    if (chatChunk.getChoices() != null && !chatChunk.getChoices().isEmpty()) {
                                        String data = chatChunk.getChoices().get(0).getDelta().getContent();
                                        String reasoningContent = chatChunk.getChoices().get(0).getDelta().getReasoningContent();
                                        if (EmptyUtil.isNotEmpty(data)) {
                                            // 对数据进行Base64编码以避免换行符干扰SSE协议
                                            String encodedData = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
                                            emitter.send(SseEmitter.event()
                                                    .data(encodedData)
                                                    .name("message"));
                                        }
                                        if(EmptyUtil.isNotEmpty(reasoningContent)){
                                            // 对推理内容进行Base64编码以避免换行符干扰SSE协议
                                            String encodedReasoningContent = Base64.getEncoder().encodeToString(reasoningContent.getBytes(StandardCharsets.UTF_8));
                                            emitter.send(SseEmitter.event()
                                                    .data(encodedReasoningContent)
                                                    .name("reasoningContent"));
                                        }
                                    } else {
                                        LogUtil.debug("收到无内容的chunk数据，可能表示流结束或控制消息: {}", content);
                                    }
                                } catch (Exception parseException) {
                                    LogUtil.warn("解析AI响应数据失败，可能是控制消息或结束信号: {}，错误: {}", content, parseException.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            LogUtil.error("发送SSE流式数据失败: {}", e.getMessage(), e);
                            // 检查是否是已完成的连接，如果是则不发送错误
                            if (!completed.get()) {
                                completed.set(true);
                                try {
                                    String errorMsg = "{\"error\":\"发送数据失败: " + e.getMessage() + "\"}";
                                    String encodedErrorMsg = Base64.getEncoder().encodeToString(errorMsg.getBytes(StandardCharsets.UTF_8));
                                    emitter.send(SseEmitter.event()
                                            .data(encodedErrorMsg)
                                            .name("error"));
                                    emitter.complete();
                                } catch (Exception ex) {
                                    LogUtil.error("发送SSE错误信号失败", ex);
                                    emitter.completeWithError(ex);
                                }
                            }
                        }
                    }
                    @Override
                    public void finish() {
                        try {
                            if (!completed.get()) {
                                completed.set(true);
                                // 发送完成事件
                                String completeMsg = "{\"status\":\"completed\"}";
                                String encodedCompleteMsg = Base64.getEncoder().encodeToString(completeMsg.getBytes(StandardCharsets.UTF_8));
                                emitter.send(SseEmitter.event()
                                        .data(encodedCompleteMsg)
                                        .name("complete"));
                                emitter.complete();
                            }
                            LogUtil.info("SSE流式聊天消息处理完成: conversationId={}", conversationId);
                        } catch (Exception e) {
                            LogUtil.error("完成SSE输出失败", e);
                            emitter.completeWithError(e);
                        }
                    }
                    @Override
                    public void onFailure() {
                        try {
                            LogUtil.error("AI服务调用失败: conversationId={}", conversationId);
                            if (!completed.get()) {
                                completed.set(true);
                                String errorMsg = "{\"error\":\"AI服务调用失败\"}";
                                String encodedErrorMsg = Base64.getEncoder().encodeToString(errorMsg.getBytes(StandardCharsets.UTF_8));
                                emitter.send(SseEmitter.event()
                                        .data(encodedErrorMsg)
                                        .name("error"));
                                emitter.complete();
                            }
                        } catch (Exception e) {
                            LogUtil.error("发送SSE失败信息失败", e);
                            emitter.completeWithError(e);
                        }
                    }
                    @Override
                    public void tortoiseMsg(String content) {
                        try {
                            if (EmptyUtil.isNotEmpty(content)) {
                                // 对tortoise消息进行Base64编码以避免换行符干扰SSE协议
                                String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
                                emitter.send(SseEmitter.event()
                                        .data(encodedContent)
                                        .name("tortoiseMsg"));
                            }
                        } catch (Exception parseException) {
                            LogUtil.warn("解析AI响应数据失败，可能是控制消息或结束信号: {}，错误: {}", content, parseException.getMessage());
                        }
                    }
                },toolNames);

            } catch (Exception e) {
                LogUtil.error("处理SSE流式聊天消息失败: conversationId={}", conversationId, e.getMessage());
                try {
                    String errorMsg = "{\"error\":\"" + e.getMessage() + "\"}";
                    String encodedErrorMsg = Base64.getEncoder().encodeToString(errorMsg.getBytes(StandardCharsets.UTF_8));
                    emitter.send(SseEmitter.event()
                            .data(encodedErrorMsg)
                            .name("error"));
                    emitter.complete();
                } catch (Exception ex) {
                    LogUtil.error("发送SSE错误信息失败", ex);
                    emitter.completeWithError(e);
                }
            }
        });

        return emitter;
    }

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping("/conversations")
    public TortoiseBaseResult<Page<TortoiseConversationDTO>> getConversations(
            @RequestParam(defaultValue = "1") @javax.validation.constraints.Min(value = 1, message = "页码必须大于0") Long pageNum,
            @RequestParam(defaultValue = "50") @javax.validation.constraints.Min(value = 1, message = "每页大小必须大于0") Long pageSize,
            @RequestParam(required = false) String conversationId) {

        try {
            LogUtil.debug("获取当前用户会话列表: pageNum={}, pageSize={}, conversationId={}",
                    pageNum, pageSize, conversationId);

            Long userId = TortoiseContext.getCurrentUserId();
            Page<TortoiseConversationDTO> page = conversationService.page(conversationId, userId, pageNum, pageSize);

            LogUtil.debug("获取当前用户会话列表成功: userId={}, total={}", userId, page.getTotal());
            return TortoiseBaseResult.ok(page);

        } catch (Exception e) {
            LogUtil.error("获取当前用户会话列表失败: userId={}", TortoiseContext.getCurrentUserId(), e);
            throw e;
        }
    }

}
