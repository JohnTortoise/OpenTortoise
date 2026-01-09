package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.dto.TortoiseLlmConfigDTO;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDTO;
import io.github.johntortoise.model.TortoiseChatProfile;

import java.util.List;


public interface TortoiseChatProfileService extends IService<TortoiseChatProfile> {

    
    Page<TortoiseChatProfile> page(String name, Long current,
                                   Long size,Boolean enable);

    
    void addOrUpdate(TortoiseChatProfile tortoiseChatProfile);

    
    TortoiseChatProfile findBySK(String sk);

    List<TortoiseLlmConfigDTO> searchModel(String keyword);

    List<TortoiseMemoryPolicyDTO> searchMemoryPolicy(String keyword);

    void refreshMemory(Long tortoiseChatProfileId);

    Boolean checkLimit(String conversationId);




}