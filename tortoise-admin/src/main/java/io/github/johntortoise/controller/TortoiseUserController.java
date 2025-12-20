package io.github.johntortoise.controller;

import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.CreateSysUserReq;
import io.github.johntortoise.service.TortoiseSysUserService;
import io.github.johntortoise.service.TortoiseUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;


@RestController
@RequestMapping("/api/users")
@Slf4j
public class TortoiseUserController {

    @Resource
    private TortoiseSysUserService sysUserService;

    @Resource
    private TortoiseUserService tortoiseUserService;

    
    @PostMapping("createSysUser")
    public ResponseEntity<Void> createSysUser(@Valid @RequestBody CreateSysUserReq createSysUserReq) {
        try {
            LogUtil.info("创建系统用户: email={}", createSysUserReq.getEmail());
            sysUserService.createUser(createSysUserReq);
            LogUtil.info("创建系统用户成功: email={}", createSysUserReq.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LogUtil.error("创建系统用户失败: email={}", createSysUserReq.getEmail(), e);
            throw e;
        }
    }

    
    @GetMapping("createUser")
    public ResponseEntity<Long> createUser(@RequestParam("customerId") @NotBlank(message = "客户ID不能为空") String customerId) {
        try {
            LogUtil.info("创建用户: customerId={}", customerId);
            Long userId = tortoiseUserService.createUser(customerId);
            LogUtil.info("创建用户成功: userId={}, customerId={}", userId, customerId);
            return ResponseEntity.ok(userId);
        } catch (Exception e) {
            LogUtil.error("创建用户失败: customerId={}", customerId, e);
            throw e;
        }
    }

}