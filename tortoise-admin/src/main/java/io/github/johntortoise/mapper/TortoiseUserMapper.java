package io.github.johntortoise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.johntortoise.model.TortoiseUser;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface TortoiseUserMapper extends BaseMapper<TortoiseUser> {

}