package io.github.johntortoise.controller;

import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.LoginRequest;
import io.github.johntortoise.dto.LoginResponse;
import io.github.johntortoise.enums.TortoiseRoleEnum;
import io.github.johntortoise.model.TortoiseSysUser;
import io.github.johntortoise.service.TortoiseSysUserService;
import io.github.johntortoise.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Objects;


@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Resource
    private TortoiseSysUserService sysUserService;

    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                               HttpServletRequest request) {
        try {
            LogUtil.info("用户登录请求: email={}", loginRequest.getEmail());
            
            
            TortoiseSysUser user = sysUserService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TortoiseSysUser>()
                    .eq("email", loginRequest.getEmail())
            );

            
            if (user == null) {
                LogUtil.warn("登录失败: 用户不存在, email={}", loginRequest.getEmail());
                return ResponseEntity.ok(LoginResponse.builder()
                    .success(false)
                    .message("用户不存在")
                    .build());
            }

            
            
            if (!Objects.equals(user.getPasswordHash(), loginRequest.getPassword())) {
                LogUtil.warn("登录失败: 密码错误, email={}", loginRequest.getEmail());
                return ResponseEntity.ok(LoginResponse.builder()
                    .success(false)
                    .message("密码错误")
                    .build());
            }

            
            String token = JwtUtil.generateToken(user.getId(), user.getEmail(), user.getRoleCode());

            
            TortoiseRoleEnum roleEnum = TortoiseRoleEnum.getById(user.getRoleCode());
            
            
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roleCode(roleEnum != null ? roleEnum.getRoleCode() : "")
                .roleName(roleEnum != null ? roleEnum.getRoleName() : "")
                .build();

            
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRoleCode());

            LogUtil.info("用户登录成功: userId={}, email={}", user.getId(), user.getEmail());
            
            
            return ResponseEntity.ok(LoginResponse.builder()
                .success(true)
                .message("登录成功")
                .token(token)
                .userInfo(userInfo)
                .build());
        } catch (IllegalArgumentException e) {
            LogUtil.warn("登录参数错误: {}", e.getMessage());
            return ResponseEntity.ok(LoginResponse.builder()
                .success(false)
                .message("登录失败: " + e.getMessage())
                .build());
        } catch (Exception e) {
            LogUtil.error("登录异常", e);
            return ResponseEntity.ok(LoginResponse.builder()
                .success(false)
                .message("登录失败，请稍后重试")
                .build());
        }
    }
    
    
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request) {
        try {
            
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object userId = session.getAttribute("userId");
                session.invalidate();
                LogUtil.info("用户登出成功: userId={}", userId);
            }
            
            
            return ResponseEntity.ok(LoginResponse.builder()
                .success(true)
                .message("登出成功")
                .build());
        } catch (Exception e) {
            LogUtil.error("登出异常", e);
            return ResponseEntity.ok(LoginResponse.builder()
                .success(false)
                .message("登出失败，请稍后重试")
                .build());
        }
    }
}