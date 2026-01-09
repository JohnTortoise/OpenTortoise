package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.dto.SaveConversationToolsReq;
import io.github.johntortoise.dto.TortoiseConversationToolDTO;
import io.github.johntortoise.model.TortoiseConversationTool;

import java.util.List;

public interface TortoiseConversationToolService extends IService<TortoiseConversationTool> {

    /**
     * 保存会话关联的工具
     */
    boolean saveConversationTools(SaveConversationToolsReq req);

    /**
     * 查询会话关联的工具
     */
    List<TortoiseConversationToolDTO> getConversationTools(String conversationId);
}









