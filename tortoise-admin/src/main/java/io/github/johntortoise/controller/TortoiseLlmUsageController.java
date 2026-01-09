package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.TortoiseLlmUsageDTO;
import io.github.johntortoise.model.TortoiseLlmUsage;
import io.github.johntortoise.service.TortoiseLlmUsageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/api/llm-usage")
@Slf4j
public class TortoiseLlmUsageController {

    @Resource
    private TortoiseLlmUsageService llmUsageService;

    
    @GetMapping("/page")
    public TortoiseBaseResult<Page<TortoiseLlmUsageDTO>> page(
            @RequestParam(required = false) String conversationId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Long pageSize) {
        try {
            LogUtil.debug("分页查询LLM使用记录: conversationId={}, pageNum={}, pageSize={}",
                    conversationId, pageNum, pageSize);
            Page<TortoiseLlmUsageDTO> page = llmUsageService.page(conversationId, pageNum, pageSize);
            return TortoiseBaseResult.ok(page);
        } catch (Exception e) {
            LogUtil.error("分页查询LLM使用记录失败: conversationId={}", conversationId, e);
            throw e;
        }
    }

    
    @GetMapping("/detail")
    public TortoiseBaseResult<TortoiseLlmUsage> getUsageDetail(
            @RequestParam @NotNull(message = "使用记录ID不能为空") Long id) {
        try {
            LogUtil.debug("获取LLM使用记录详情: id={}", id);
            TortoiseLlmUsage usage = llmUsageService.getById(id);
            if (usage == null) {
                LogUtil.warn("LLM使用记录不存在: id={}", id);
            }
            return TortoiseBaseResult.ok(usage);
        } catch (Exception e) {
            LogUtil.error("获取LLM使用记录详情失败: id={}", id, e);
            throw e;
        }
    }

}