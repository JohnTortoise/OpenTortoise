package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.model.TortoiseUser;


public interface TortoiseUserService extends IService<TortoiseUser> {

   Long createUser(String customerId);

}