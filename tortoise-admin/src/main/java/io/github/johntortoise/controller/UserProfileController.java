package io.github.johntortoise.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.context.TortoiseContext;
import io.github.johntortoise.dto.CreateSysUserReq;
import io.github.johntortoise.dto.UpdateSysUserReq;
import io.github.johntortoise.dto.UserProfileDTO;
import io.github.johntortoise.enums.TortoiseRoleEnum;
import io.github.johntortoise.model.TortoiseSysUser;
import io.github.johntortoise.service.TortoiseSysUserService;
import io.github.johntortoise.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserProfileController {

    @Autowired
    private TortoiseSysUserService sysUserService;

    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LogUtil.debug("获取当前用户信息");
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                LogUtil.warn("未找到用户信息");
                response.put("success", false);
                response.put("message", "未找到用户信息");
                return ResponseEntity.ok(response);
            }
            
            UserProfileDTO profile = sysUserService.getUserProfile(userId);
            if (profile == null) {
                LogUtil.warn("用户不存在: userId={}", userId);
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }
            
            response.put("success", true);
            response.put("data", profile);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtil.error("获取用户信息失败", e);
            response.put("success", false);
            response.put("message", "获取用户信息失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    
    @PostMapping("/profile/update")
    public ResponseEntity<Map<String, Object>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateSysUserReq updateReq,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LogUtil.debug("更新当前用户信息");
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                LogUtil.warn("未找到用户信息");
                response.put("success", false);
                response.put("message", "未找到用户信息");
                return ResponseEntity.ok(response);
            }
            
            updateReq.setId(userId);
            boolean success = sysUserService.updateUser(updateReq);
            
            if (success) {
                LogUtil.info("更新用户信息成功: userId={}", userId);
            } else {
                LogUtil.warn("更新用户信息失败: userId={}", userId);
            }
            
            response.put("success", success);
            response.put("message", success ? "更新成功" : "更新失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtil.error("更新用户信息失败", e);
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @RequestParam(required = false) String email,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LogUtil.debug("获取用户列表: email={}, current={}, size={}", email, current, size);
            
            
            if (!isAdmin(request)) {
                LogUtil.warn("无权限访问用户列表");
                response.put("success", false);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            Page<TortoiseSysUser> page = new Page<>(current, size);
            IPage<UserProfileDTO> userPage = sysUserService.getUserList(page, email);
            
            response.put("success", true);
            response.put("data", userPage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtil.error("获取用户列表失败: email={}", email, e);
            response.put("success", false);
            response.put("message", "获取用户列表失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody CreateSysUserReq createReq,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LogUtil.info("创建用户: email={}", createReq.getEmail());
            
            
            if (!isAdmin(request)) {
                LogUtil.warn("无权限创建用户");
                response.put("success", false);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            sysUserService.createUser(createReq);

            LogUtil.info("创建用户成功: email={}", createReq.getEmail());
            response.put("success", true);
            response.put("message", "创建成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtil.error("创建用户失败: email={}", createReq.getEmail(), e);
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(
            @Valid @RequestBody UpdateSysUserReq updateReq,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LogUtil.info("更新用户: id={}", updateReq.getId());
            
            
            if (!isAdmin(request)) {
                LogUtil.warn("无权限更新用户");
                response.put("success", false);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            boolean success = sysUserService.updateUser(updateReq);
            
            if (success) {
                LogUtil.info("更新用户成功: id={}", updateReq.getId());
            } else {
                LogUtil.warn("更新用户失败: id={}", updateReq.getId());
            }
            
            response.put("success", success);
            response.put("message", success ? "更新成功" : "更新失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtil.error("更新用户失败: id={}", updateReq.getId(), e);
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LogUtil.info("删除用户: userId={}", userId);
            
            
            if (!isAdmin(request)) {
                LogUtil.warn("无权限删除用户");
                response.put("success", false);
                response.put("message", "无权限访问");
                return ResponseEntity.ok(response);
            }
            
            boolean success = sysUserService.deleteUser(userId);
            
            if (success) {
                LogUtil.info("删除用户成功: userId={}", userId);
            } else {
                LogUtil.warn("删除用户失败: userId={}", userId);
            }
            
            response.put("success", success);
            response.put("message", success ? "删除成功" : "删除失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtil.error("删除用户失败: userId={}", userId, e);
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    
    private Long getCurrentUserId(HttpServletRequest request) {
        
        Long userId = TortoiseContext.getCurrentUserId();
        if (userId != null) {
            return userId;
        }
        
        
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj != null) {
            return (Long) userIdObj;
        }
        
        
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                return JwtUtil.getUserIdFromToken(token);
            }
        }
        
        return null;
    }

    
    private boolean isAdmin(HttpServletRequest request) {
        Long roleCode = getCurrentUserRoleCode(request);
        if (roleCode == null) {
            return false;
        }
        
        
        TortoiseRoleEnum roleEnum = TortoiseRoleEnum.getById(roleCode);
        return roleEnum != null && (roleEnum == TortoiseRoleEnum.SUPER_ADMIN);
    }

    
    private Long getCurrentUserRoleCode(HttpServletRequest request) {
        
        Object roleCodeObj = request.getAttribute("roleCode");
        if (roleCodeObj != null) {
            return (Long) roleCodeObj;
        }
        
        
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                return JwtUtil.getRoleCodeFromToken(token);
            }
        }
        
        
        Long userId = getCurrentUserId(request);
        if (userId != null) {
            TortoiseSysUser user = sysUserService.getById(userId);
            if (user != null) {
                return user.getRoleCode();
            }
        }
        
        return null;
    }
}

