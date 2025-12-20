package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.context.TortoiseContext;
import io.github.johntortoise.dto.TortoiseLlmConfigDTO;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDTO;
import io.github.johntortoise.model.TortoiseChatProfile;
import io.github.johntortoise.service.TortoiseChatProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/api/chatProfile")
public class TortoiseChatProfileController {

    @Resource
    private TortoiseChatProfileService tortoiseChatProfileService;

    
    @GetMapping("/page")
    public ResponseEntity<Page<TortoiseChatProfile>> page(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Long pageSize) {
        try {
            LogUtil.debug("分页查询聊天配置: name={}, pageNum={}, pageSize={}", name, pageNum, pageSize);
            return ResponseEntity.ok(tortoiseChatProfileService.page(name, pageNum, pageSize));
        } catch (Exception e) {
            LogUtil.error("分页查询聊天配置失败: name={}", name, e);
            throw e;
        }
    }

    
    @PostMapping("addOrUpdate")
    public ResponseEntity<Void> addOrUpdate(@Valid @RequestBody TortoiseChatProfile tortoiseChatProfile) {
        try {
            tortoiseChatProfile.setCreateUserId(TortoiseContext.getCurrentUserId());
            LogUtil.info("新增或更新聊天配置: id={}, name={}",
                    tortoiseChatProfile.getId(), tortoiseChatProfile.getName());
            tortoiseChatProfileService.addOrUpdate(tortoiseChatProfile);
            LogUtil.info("新增或更新聊天配置成功: id={}", tortoiseChatProfile.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LogUtil.error("新增或更新聊天配置失败: id={}", tortoiseChatProfile.getId(), e);
            throw e;
        }
    }

    
    @GetMapping("searchModel")
    public ResponseEntity<List<TortoiseLlmConfigDTO>> searchModel(
            @RequestParam("keyword") String keyword) {
        try {
            LogUtil.debug("搜索模型: keyword={}", keyword);
            return ResponseEntity.ok(tortoiseChatProfileService.searchModel(keyword));
        } catch (Exception e) {
            LogUtil.error("搜索模型失败: keyword={}", keyword, e);
            throw e;
        }
    }

    
    @GetMapping("searchMemoryPolicy")
    public ResponseEntity<List<TortoiseMemoryPolicyDTO>> searchMemoryPolicy(
            @RequestParam("keyword") String keyword) {
        try {
            LogUtil.debug("搜索记忆策略: keyword={}", keyword);
            return ResponseEntity.ok(tortoiseChatProfileService.searchMemoryPolicy(keyword));
        } catch (Exception e) {
            LogUtil.error("搜索记忆策略失败: keyword={}", keyword, e);
            throw e;
        }
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<TortoiseChatProfile> getById(@PathVariable Long id) {
        try {
            LogUtil.debug("获取聊天配置详情: id={}", id);
            TortoiseChatProfile profile = tortoiseChatProfileService.getById(id);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            LogUtil.error("获取聊天配置详情失败: id={}", id, e);
            throw e;
        }
    }

    
    @GetMapping("refreshMemory")
    public ResponseEntity<Void> refreshMemory(@RequestParam Long id){
        tortoiseChatProfileService.refreshMemory(id);
        return ResponseEntity.ok(null);
    }

}