package io.github.johntortoise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.johntortoise.dto.TortoiseConversationToolDTO;
import io.github.johntortoise.model.TortoiseConversationTool;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TortoiseConversationToolMapper extends BaseMapper<TortoiseConversationTool> {

}
