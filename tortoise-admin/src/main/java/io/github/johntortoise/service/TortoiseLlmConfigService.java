package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.dto.TortoiseLlmConfigDTO;
import io.github.johntortoise.model.TortoiseLlmConfig;


public interface TortoiseLlmConfigService extends IService<TortoiseLlmConfig> {
    
    
    Page<TortoiseLlmConfig> getConfigListByPage(Long current, Long size, String modelName, String configName);
    
    
    Page<TortoiseLlmConfigDTO> getConfigListByPageAsDTO(Long current, Long size, String modelName, String configName);
    
    
    void saveOrUpdateConfig(TortoiseLlmConfig config);


}