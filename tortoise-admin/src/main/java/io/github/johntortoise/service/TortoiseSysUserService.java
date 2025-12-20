package io.github.johntortoise.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.dto.CreateSysUserReq;
import io.github.johntortoise.dto.UpdateSysUserReq;
import io.github.johntortoise.dto.UserProfileDTO;
import io.github.johntortoise.model.TortoiseSysUser;


public interface TortoiseSysUserService extends IService<TortoiseSysUser> {

   void createUser(CreateSysUserReq createSysUserReq);
   
   UserProfileDTO getUserProfile(Long userId);
   
   IPage<UserProfileDTO> getUserList(Page<TortoiseSysUser> page, String email);
   
   boolean updateUser(UpdateSysUserReq updateReq);
   
   boolean deleteUser(Long userId);
}