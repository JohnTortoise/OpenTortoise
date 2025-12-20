package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDTO;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDetailDTO;
import io.github.johntortoise.service.TortoiseMemoryPolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/api/policy")
@Slf4j
public class TortoiseMemoryPolicyController {

    @Resource
    private TortoiseMemoryPolicyService tortoiseMemoryPolicyService;

    
    @PostMapping("add")
    public ResponseEntity<Void> addOrUpdate(@Valid @RequestBody TortoiseMemoryPolicyDetailDTO tortoiseMemoryPolicyDetailDTO) {
        try {
            LogUtil.info("新增记忆策略: name={}", tortoiseMemoryPolicyDetailDTO.getName());
            tortoiseMemoryPolicyService.addOrUpdate(tortoiseMemoryPolicyDetailDTO);
            LogUtil.info("新增记忆策略成功");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LogUtil.error("新增记忆策略失败: name={}", tortoiseMemoryPolicyDetailDTO.getName(), e);
            throw e;
        }
    }

    
    @GetMapping("detail")
    public ResponseEntity<TortoiseMemoryPolicyDetailDTO> detail(
            @RequestParam @NotNull(message = "策略ID不能为空") Long id) {
        try {
            LogUtil.debug("获取记忆策略详情: id={}", id);
            TortoiseMemoryPolicyDetailDTO detail = tortoiseMemoryPolicyService.detail(id);
            if (detail == null) {
                LogUtil.warn("记忆策略不存在: id={}", id);
            }
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            LogUtil.error("获取记忆策略详情失败: id={}", id, e);
            throw e;
        }
    }

    
    @GetMapping("/page")
    public ResponseEntity<Page<TortoiseMemoryPolicyDTO>> page(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Long pageSize) {
        try {
            LogUtil.debug("分页查询记忆策略: name={}, pageNum={}, pageSize={}", name, pageNum, pageSize);
            Page<TortoiseMemoryPolicyDTO> page = tortoiseMemoryPolicyService.page(name, pageNum, pageSize);
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            LogUtil.error("分页查询记忆策略失败: name={}", name, e);
            throw e;
        }
    }

}