package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.mapper.TortoiseMemoryPromptConfigMapper;
import io.github.johntortoise.model.TortoiseMemoryPromptConfig;
import io.github.johntortoise.service.TortoiseMemoryPromptConfigService;
import org.springframework.stereotype.Service;

@Service
public class TortoiseMemoryPromptConfigServiceImpl extends ServiceImpl<TortoiseMemoryPromptConfigMapper, TortoiseMemoryPromptConfig> implements TortoiseMemoryPromptConfigService {

    @Override
    public TortoiseMemoryPromptConfig findPrompt(Integer type) {
        return this.getOne(new QueryWrapper<TortoiseMemoryPromptConfig>().lambda()
                .eq(TortoiseMemoryPromptConfig::getType,type)
                .orderByDesc(TortoiseMemoryPromptConfig::getCreateTime)
                .last("limit 1"));
    }
}
