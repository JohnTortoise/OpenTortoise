package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.model.TortoiseMemoryPromptConfig;


public interface TortoiseMemoryPromptConfigService extends IService<TortoiseMemoryPromptConfig> {

    TortoiseMemoryPromptConfig findPrompt(Integer type);


}