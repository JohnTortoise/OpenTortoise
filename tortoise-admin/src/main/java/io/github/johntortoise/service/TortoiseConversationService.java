package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.model.TortoiseConversation;

import java.util.List;


public interface TortoiseConversationService extends IService<TortoiseConversation> {
    Page<TortoiseConversationDTO> page(String conversationId, Long userId, Long current,
                                       Long size);
    
    
    TortoiseConversation createConversation(Long userId, Long chatProfileId);

    TortoiseConversationDTO getByConversationId(String conversationId);

    
    TortoiseConversation getMinIdByChatProfileId(Long chatProfileId);

    
    TortoiseConversation getMaxIdByChatProfileId(Long chatProfileId);

    
    List<TortoiseConversation> listByChatProfileIdAndIdRange(Long chatProfileId, Long minId, Long maxId);

    
    void importExcel(Long recordId,String tempFilePath, String originalFilename);

    String copyConversation(String conversationId);

}