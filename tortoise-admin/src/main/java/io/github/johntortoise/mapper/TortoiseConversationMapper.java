package io.github.johntortoise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.johntortoise.model.TortoiseConversation;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface TortoiseConversationMapper extends BaseMapper<TortoiseConversation> {
}