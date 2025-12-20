package io.github.johntortoise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.johntortoise.model.TortoiseChatProfile;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface TortoiseChatProfileMapper extends BaseMapper<TortoiseChatProfile> {
}