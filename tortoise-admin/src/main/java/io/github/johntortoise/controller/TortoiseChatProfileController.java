package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
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
    public TortoiseBaseResult<Page<TortoiseChatProfile>> page(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Long pageSize,
            @RequestParam(defaultValue = "") Boolean enable) {
        try {
            LogUtil.debug("分页查询聊天配置: name={}, pageNum={}, pageSize={}", name, pageNum, pageSize);
            return TortoiseBaseResult.ok(tortoiseChatProfileService.page(name, pageNum, pageSize,enable));
        } catch (Exception e) {
            LogUtil.error("分页查询聊天配置失败: name={}", name, e);
            throw e;
        }
    }

    
    @PostMapping("addOrUpdate")
    public TortoiseBaseResult<Void> addOrUpdate(@Valid @RequestBody TortoiseChatProfile tortoiseChatProfile) {
        try {
            tortoiseChatProfile.setCreateUserId(TortoiseContext.getCurrentUserId());
            LogUtil.info("新增或更新聊天配置: id={}, name={}",
                    tortoiseChatProfile.getId(), tortoiseChatProfile.getName());
            tortoiseChatProfileService.addOrUpdate(tortoiseChatProfile);
            LogUtil.info("新增或更新聊天配置成功: id={}", tortoiseChatProfile.getId());
            return TortoiseBaseResult.ok();
        } catch (Exception e) {
            LogUtil.error("新增或更新聊天配置失败: id={}", tortoiseChatProfile.getId(), e);
            throw e;
        }
    }

    
    @GetMapping("searchModel")
    public TortoiseBaseResult<List<TortoiseLlmConfigDTO>> searchModel(
            @RequestParam("keyword") String keyword) {
        try {
            LogUtil.debug("搜索模型: keyword={}", keyword);
            return TortoiseBaseResult.ok(tortoiseChatProfileService.searchModel(keyword));
        } catch (Exception e) {
            LogUtil.error("搜索模型失败: keyword={}", keyword, e);
            throw e;
        }
    }

    
    @GetMapping("searchMemoryPolicy")
    public TortoiseBaseResult<List<TortoiseMemoryPolicyDTO>> searchMemoryPolicy(
            @RequestParam("keyword") String keyword) {
        try {
            LogUtil.debug("搜索记忆策略: keyword={}", keyword);
            return TortoiseBaseResult.ok(tortoiseChatProfileService.searchMemoryPolicy(keyword));
        } catch (Exception e) {
            LogUtil.error("搜索记忆策略失败: keyword={}", keyword, e);
            throw e;
        }
    }

    
    @GetMapping("/{id}")
    public TortoiseBaseResult<TortoiseChatProfile> getById(@PathVariable Long id) {
        try {
            LogUtil.debug("获取聊天配置详情: id={}", id);
            TortoiseChatProfile profile = tortoiseChatProfileService.getById(id);
            if (profile == null) {
                return TortoiseBaseResult.fail("查询不到聊天配置");
            }
            return TortoiseBaseResult.ok(profile);
        } catch (Exception e) {
            LogUtil.error("获取聊天配置详情失败: id={}", id, e);
            throw e;
        }
    }

    
    @GetMapping("refreshMemory")
    public TortoiseBaseResult<Void> refreshMemory(@RequestParam Long id){
        tortoiseChatProfileService.refreshMemory(id);
        return TortoiseBaseResult.ok();
    }

}