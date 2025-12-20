package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.dto.MemoryBase;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDTO;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDetailDTO;
import io.github.johntortoise.model.TortoiseMemoryPolicy;

public interface TortoiseMemoryPolicyService extends IService<TortoiseMemoryPolicy> {

   Page<TortoiseMemoryPolicyDTO> page(String name, Long current,
                                      Long size);
   TortoiseMemoryPolicyDetailDTO detail(Long id);

   void addOrUpdate(TortoiseMemoryPolicyDetailDTO tortoiseMemoryPolicyDetailDTO);

   MemoryBase autoFixIfPromptIsEmpty(MemoryBase memoryBase);

   
   void memoryReconstructionByPolicy(String conversationId,Long memoryPolicyId);
}
