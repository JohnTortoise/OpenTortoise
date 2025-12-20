package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.mapper.TortoiseUserMapper;
import io.github.johntortoise.model.TortoiseUser;
import io.github.johntortoise.service.TortoiseUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class TortoiseUserServiceImpl extends ServiceImpl<TortoiseUserMapper, TortoiseUser> implements TortoiseUserService {
    
    public Long createUser(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("客户ID不能为空");
        }
        
        try {
            TortoiseUser tortoiseUser = new TortoiseUser();
            tortoiseUser.setCustomId(customerId);
            this.save(tortoiseUser);
            LogUtil.info("创建用户成功: userId={}, customerId={}", tortoiseUser.getId(), customerId);
            return tortoiseUser.getId();
        } catch (Exception e) {
            LogUtil.error("创建用户失败: customerId={}", customerId, e);
            throw e;
        }
    }
}