package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.dto.TortoiseLlmUsageDTO;
import io.github.johntortoise.enums.TortoiseLlmUsageEnum;
import io.github.johntortoise.model.TortoiseLlmUsage;

import java.util.List;


public interface TortoiseLlmUsageService extends IService<TortoiseLlmUsage> {
    
    List<TortoiseLlmUsage> getUsageByConversationId(String conversationId);

    
    Page<TortoiseLlmUsageDTO> page(String conversationId, Long current, Long size);

    
    void recordUsage(AfterChatDTO afterChatDTO, TortoiseLlmUsageEnum tortoiseLlmUsageEnum);

    TortoiseLlmUsage queryFirstUsageByLLmConfigId(Long llmConfigId);
}