package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.context.TortoiseContext;
import io.github.johntortoise.dto.TortoiseLlmConfigDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.mapper.TortoiseLlmConfigMapper;
import io.github.johntortoise.model.TortoiseLlmConfig;
import io.github.johntortoise.model.TortoiseLlmUsage;
import io.github.johntortoise.service.TortoiseLlmConfigService;
import io.github.johntortoise.service.TortoiseLlmUsageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TortoiseLlmConfigServiceImpl extends ServiceImpl<TortoiseLlmConfigMapper, TortoiseLlmConfig> implements TortoiseLlmConfigService {


    @Resource
    @Lazy
    private TortoiseLlmUsageService tortoiseLlmUsageService;

    @Override
    public Page<TortoiseLlmConfig> getConfigListByPage(Long current, Long size, String modelName, String configName) {
        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        Page<TortoiseLlmConfig> page = new Page<>(current, size);

        LambdaQueryWrapper<TortoiseLlmConfig> queryWrapper = new LambdaQueryWrapper<>();
        
        
        if (modelName != null && !modelName.trim().isEmpty()) {
            queryWrapper.like(TortoiseLlmConfig::getModelName, modelName);
        }
        if (configName != null && !configName.trim().isEmpty()) {
            queryWrapper.like(TortoiseLlmConfig::getConfigName, configName);
        }
        
        
        queryWrapper.eq(TortoiseLlmConfig::getIsDeleted, DeletedEnum.EXIST.getCode());
        
        
        queryWrapper.orderByDesc(TortoiseLlmConfig::getCreateTime);
        
        
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<TortoiseLlmConfigDTO> getConfigListByPageAsDTO(Long current, Long size, String modelName, String configName) {
        
        Page<TortoiseLlmConfig> page = getConfigListByPage(current, size, modelName, configName);
        
        
        Page<TortoiseLlmConfigDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        
        
        dtoPage.setRecords(page.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));
        
        return dtoPage;
    }
    
    
    private TortoiseLlmConfigDTO convertToDTO(TortoiseLlmConfig config) {
        TortoiseLlmConfigDTO dto = new TortoiseLlmConfigDTO();
        dto.setId(config.getId());
        dto.setConfigName(config.getConfigName());
        dto.setModelName(config.getModelName());
        dto.setApiUrl(config.getApiUrl());
        dto.setStatus(config.getStatus());
        dto.setDescription(config.getDescription());
        dto.setCreateTime(config.getCreateTime());
        return dto;
    }
    
    @Override
    public void saveOrUpdateConfig(TortoiseLlmConfig config) {
        if(Objects.nonNull(config.getId())){
            TortoiseLlmConfig configDB = this.getById(config.getId());
            BigDecimal inputUnitPrice = configDB.getInputUnitPrice();
            BigDecimal outputUnitPrice = configDB.getOutputUnitPrice();
            BigDecimal cachePrice = configDB.getCachePrice();

            if (isBigDecimalChanged(config.getInputUnitPrice(), inputUnitPrice)
                    || isBigDecimalChanged(config.getOutputUnitPrice(), outputUnitPrice)
                    || isBigDecimalChanged(config.getCachePrice(), cachePrice))  {
                TortoiseLlmUsage tortoiseLlmUsage = tortoiseLlmUsageService.queryFirstUsageByLLmConfigId(config.getId());
                if(Objects.nonNull(tortoiseLlmUsage)){
                    throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, String.format("该模型已被会话id%s使用过，无法变更价格",tortoiseLlmUsage.getConversationId()));
                }
            }
        }
        if (EmptyUtil.isEmpty(config)) {
            throw new IllegalArgumentException("配置对象不能为空");
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            config.setCreateUserId(TortoiseContext.getCurrentUserId());
            config.setUpdateTime(now);
            this.saveOrUpdate(config);
            LogUtil.info("保存或更新LLM配置成功: id={}", config.getId());
        } catch (Exception e) {
            LogUtil.error("保存或更新LLM配置失败: id={}, name={}", config.getId(), config.getConfigName(), e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "保存或更新配置失败", e);
        }
    }


    private boolean isBigDecimalChanged(BigDecimal newVal, BigDecimal oldVal) {
        if (newVal == null && oldVal == null) return false;
        if (newVal == null || oldVal == null) return true;
        return newVal.compareTo(oldVal) != 0;
    }

}