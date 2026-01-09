package io.github.johntortoise.controller;

import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.service.TortoiseMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.List;


@RestController
@RequestMapping("/api/memories")
@Slf4j
public class TortoiseMemoryController {

    @Resource
    private TortoiseMemoryService memoryService;

    
    @GetMapping("/getMemoriesByConversationId")
    public TortoiseBaseResult<List<Message>> getMemoriesByConversationId(
            @RequestParam @NotBlank(message = "对话ID不能为空") String conversationId) {
        try {
            LogUtil.debug("获取记忆: conversationId={}", conversationId);
            List<Message> memories = memoryService.getMemoriesByConversationId(conversationId);
            return TortoiseBaseResult.ok(memories);
        } catch (Exception e) {
            LogUtil.error("获取记忆失败: conversationId={}", conversationId, e);
            throw e;
        }
    }
}