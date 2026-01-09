package io.github.johntortoise.controller;

import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.SaveConversationToolsReq;
import io.github.johntortoise.dto.TortoiseConversationToolDTO;
import io.github.johntortoise.service.TortoiseConversationToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/conversation/tool")
public class TortoiseConversationToolController {

    @Resource
    private TortoiseConversationToolService conversationToolService;

    /**
     * 保存会话关联的工具
     */
    @PostMapping("/save")
    public TortoiseBaseResult<Boolean> saveConversationTools(@RequestBody @Valid SaveConversationToolsReq req) {
        try {
            LogUtil.info("保存会话工具关联: conversationId={}, toolIds={}", req.getConversationId(), req.getToolIds());

            boolean result = conversationToolService.saveConversationTools(req);

            LogUtil.info("保存会话工具关联成功: conversationId={}", req.getConversationId());
            return TortoiseBaseResult.ok(result);

        } catch (Exception e) {
            LogUtil.error("保存会话工具关联失败: conversationId={}", req.getConversationId(), e);
            throw e;
        }
    }

    /**
     * 查询会话关联的工具
     */
    @GetMapping("/list")
    public TortoiseBaseResult<List<TortoiseConversationToolDTO>> getConversationTools(
            @RequestParam @NotBlank(message = "会话ID不能为空") String conversationId) {
        try {
            LogUtil.debug("查询会话工具关联: conversationId={}", conversationId);

            List<TortoiseConversationToolDTO> tools = conversationToolService.getConversationTools(conversationId);

            LogUtil.debug("查询会话工具关联成功: conversationId={}, toolCount={}", conversationId, tools.size());
            return TortoiseBaseResult.ok(tools);

        } catch (Exception e) {
            LogUtil.error("查询会话工具关联失败: conversationId={}", conversationId, e);
            throw e;
        }
    }
}









