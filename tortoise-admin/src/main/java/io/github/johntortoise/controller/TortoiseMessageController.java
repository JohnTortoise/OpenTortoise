package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.LLmInvokeResp;
import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.MessageDTO;
import io.github.johntortoise.service.TortoiseMessageExtendService;
import io.github.johntortoise.service.TortoiseMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;


@RestController
@RequestMapping("/api/messages")
@Slf4j
public class TortoiseMessageController {

    @Resource
    private TortoiseMessageService tortoiseMessageService;

    @Resource
    private TortoiseMessageExtendService tortoiseMessageExtendService;

    
    @GetMapping("/page")
    public TortoiseBaseResult<Page<MessageDTO>> page(
            @RequestParam @NotBlank(message = "对话ID不能为空") String conversationId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Long pageSize) {
        try {
            LogUtil.debug("分页查询消息: conversationId={}, pageNum={}, pageSize={}",
                    conversationId, pageNum, pageSize);
            Page<MessageDTO> page = tortoiseMessageService.page(conversationId, pageNum, pageSize);
            return TortoiseBaseResult.ok(page);
        } catch (Exception e) {
            LogUtil.error("分页查询消息失败: conversationId={}", conversationId, e);
            throw e;
        }
    }

    @GetMapping("getEventsByMessageId")
    public TortoiseBaseResult<List<LLmInvokeResp.Event>> getEventsByMessageId(@RequestParam @NotBlank(message = "消息ID不能为空") Long messageId){
        return TortoiseBaseResult.ok(tortoiseMessageExtendService.getByMessageId(messageId));
    }

}
