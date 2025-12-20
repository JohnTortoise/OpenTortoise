package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.TortoiseLlmConfigDTO;
import io.github.johntortoise.model.TortoiseLlmConfig;
import io.github.johntortoise.service.TortoiseLlmConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/api/llm-configs")
@Slf4j
public class TortoiseLlmConfigController {
    
    @Resource
    private TortoiseLlmConfigService llmConfigService;

    
    @GetMapping("/page")
    public ResponseEntity<Page<TortoiseLlmConfigDTO>> getConfigListByPage(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Long pageSize,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String configName) {
        try {
            LogUtil.debug("分页查询LLM配置: modelName={}, configName={}, pageNum={}, pageSize={}",
                    modelName, configName, pageNum, pageSize);
            return ResponseEntity.ok(llmConfigService.getConfigListByPageAsDTO(pageNum, pageSize, modelName, configName));
        } catch (Exception e) {
            LogUtil.error("分页查询LLM配置失败: modelName={}, configName={}", modelName, configName, e);
            throw e;
        }
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<TortoiseLlmConfig> getConfigById(@PathVariable @NotNull(message = "配置ID不能为空") Long id) {
        try {
            LogUtil.debug("获取配置详情: id={}", id);
            TortoiseLlmConfig config = llmConfigService.getById(id);
            if (config == null) {
                LogUtil.warn("配置不存在: id={}", id);
            }
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            LogUtil.error("获取配置详情失败: id={}", id, e);
            throw e;
        }
    }

    
    @PostMapping("saveOrUpdateConfig")
    public ResponseEntity<Void> saveOrUpdateConfig(@Valid @RequestBody TortoiseLlmConfig config) {
        try {
            LogUtil.info("保存或更新配置: id={}, name={}", config.getId(), config.getConfigName());
            llmConfigService.saveOrUpdateConfig(config);
            LogUtil.info("保存或更新配置成功: id={}", config.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LogUtil.error("保存或更新配置失败: id={}", config.getId(), e);
            throw e;
        }
    }

}