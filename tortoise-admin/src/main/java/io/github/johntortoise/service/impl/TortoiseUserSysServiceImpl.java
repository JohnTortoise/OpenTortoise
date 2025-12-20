package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.CreateSysUserReq;
import io.github.johntortoise.dto.UpdateSysUserReq;
import io.github.johntortoise.dto.UserProfileDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.enums.TortoiseRoleEnum;
import io.github.johntortoise.mapper.TortoiseSysUserMapper;
import io.github.johntortoise.model.TortoiseSysUser;
import io.github.johntortoise.service.TortoiseSysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;


@Service
@Slf4j
public class TortoiseUserSysServiceImpl extends ServiceImpl<TortoiseSysUserMapper, TortoiseSysUser> implements TortoiseSysUserService {
    
    @Override
    public void createUser(CreateSysUserReq createSysUserReq) {
        if (createSysUserReq == null) {
            throw new IllegalArgumentException("创建用户请求参数不能为空");
        }
        if (createSysUserReq.getEmail() == null || createSysUserReq.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (createSysUserReq.getPassword() == null || createSysUserReq.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (createSysUserReq.getRoleId() == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        try {
            TortoiseSysUser one = this.getOne(new QueryWrapper<TortoiseSysUser>().lambda()
                    .eq(TortoiseSysUser::getEmail, createSysUserReq.getEmail()));
            if (Objects.nonNull(one)) {
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "邮箱已存在");
            }

            TortoiseSysUser tortoiseSysUser = new TortoiseSysUser();
            tortoiseSysUser.setRoleCode(createSysUserReq.getRoleId());
            tortoiseSysUser.setEmail(createSysUserReq.getEmail());
            tortoiseSysUser.setPasswordHash(createSysUserReq.getPassword());
            this.save(tortoiseSysUser);
            LogUtil.info("创建系统用户成功: email={}, roleId={}", createSysUserReq.getEmail(), createSysUserReq.getRoleId());
        } catch (TortoiseBusinessException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error("创建系统用户失败: email={}", createSysUserReq.getEmail(), e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "创建用户失败", e);
        }
    }
    
    @Override
    public UserProfileDTO getUserProfile(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        TortoiseSysUser user = this.getById(userId);
        if (user == null) {
            LogUtil.warn("用户不存在: userId={}", userId);
            return null;
        }
        
        TortoiseRoleEnum roleEnum = TortoiseRoleEnum.getById(user.getRoleCode());
        
        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roleCode(user.getRoleCode())
                .roleName(roleEnum != null ? roleEnum.getRoleName() : "")
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
    
    @Override
    public IPage<UserProfileDTO> getUserList(Page<TortoiseSysUser> page, String email) {
        LambdaQueryWrapper<TortoiseSysUser> lambda = new QueryWrapper<TortoiseSysUser>().lambda();
        lambda.eq(TortoiseSysUser::getIsDeleted, DeletedEnum.EXIST.getCode());
        if (StringUtils.hasText(email)) {
            lambda.like(TortoiseSysUser::getEmail, email);
        }
        lambda.orderByDesc(TortoiseSysUser::getCreateTime);
        
        IPage<TortoiseSysUser> userPage = this.page(page, lambda);
        
        
        return userPage.convert(user -> {
            TortoiseRoleEnum roleEnum = TortoiseRoleEnum.getById(user.getRoleCode());
            return UserProfileDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .roleCode(user.getRoleCode())
                    .roleName(roleEnum != null ? roleEnum.getRoleName() : "")
                    .createTime(user.getCreateTime())
                    .updateTime(user.getUpdateTime())
                    .build();
        });
    }
    
    @Override
    public boolean updateUser(UpdateSysUserReq updateReq) {
        if (updateReq == null) {
            throw new IllegalArgumentException("更新用户请求参数不能为空");
        }
        if (updateReq.getId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        TortoiseSysUser user = this.getById(updateReq.getId());
        if (user == null) {
            LogUtil.warn("更新用户失败: 用户不存在, userId={}", updateReq.getId());
            return false;
        }
        
        if (StringUtils.hasText(updateReq.getEmail())) {
            user.setEmail(updateReq.getEmail());
        }
        if (StringUtils.hasText(updateReq.getPassword())) {
            user.setPasswordHash(updateReq.getPassword());
        }
        if (updateReq.getRoleId() != null) {
            user.setRoleCode(updateReq.getRoleId());
        }
        
        boolean result = this.updateById(user);
        if (result) {
            LogUtil.info("更新用户成功: userId={}", updateReq.getId());
        } else {
            LogUtil.warn("更新用户失败: userId={}", updateReq.getId());
        }
        return result;
    }
    
    @Override
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        TortoiseSysUser user = this.getById(userId);
        if (user == null) {
            LogUtil.warn("删除用户失败: 用户不存在, userId={}", userId);
            return false;
        }
        
        user.setIsDeleted(DeletedEnum.NO_EXIST.getCode());
        boolean result = this.updateById(user);
        if (result) {
            LogUtil.info("删除用户成功: userId={}", userId);
        } else {
            LogUtil.warn("删除用户失败: userId={}", userId);
        }
        return result;
    }
}