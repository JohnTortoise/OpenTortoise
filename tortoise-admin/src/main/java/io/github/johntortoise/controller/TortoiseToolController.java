package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.*;
import io.github.johntortoise.service.TortoiseToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/tool")
@Slf4j
public class TortoiseToolController {

    @Resource
    private TortoiseToolService tortoiseToolService;

    @PostMapping("parseCUrl")
    private TortoiseBaseResult<ParseCUrlDTO> parseCUrl(@RequestBody  ParseCUrlReq parseCUrlReq){
        return TortoiseBaseResult.ok(tortoiseToolService.parseCurl(parseCUrlReq));
    }



    @PostMapping("createTool")
    private TortoiseBaseResult<Boolean> createTool(@RequestBody CreateToolDTO createToolDTO){
        return TortoiseBaseResult.ok(tortoiseToolService.createTool(createToolDTO));
    }

    @PostMapping("updateTool")
    private TortoiseBaseResult<Boolean> updateTool(@RequestBody TortoiseToolDetailDTO updateToolDTO){
        return TortoiseBaseResult.ok(tortoiseToolService.updateTool(updateToolDTO));
    }

    @GetMapping("/page")
    public TortoiseBaseResult<Page<TortoiseToolDTO>> page(
            @RequestParam(required = false) String toolName,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Long pageSize) {
        try {
            LogUtil.debug("分页查询工具: toolName={}, pageNum={}, pageSize={}",
                    toolName, pageNum, pageSize);
            Page<TortoiseToolDTO> page = tortoiseToolService.page(toolName, pageNum, pageSize);
            return TortoiseBaseResult.ok(page);
        } catch (Exception e) {
            LogUtil.error("分页查询工具失败: toolName={}", toolName, e);
            throw e;
        }
    }

    @GetMapping("detail")
    public TortoiseBaseResult<TortoiseToolDetailDTO> detail(Long id){
        return TortoiseBaseResult.ok(tortoiseToolService.detail(id));
    }


}
