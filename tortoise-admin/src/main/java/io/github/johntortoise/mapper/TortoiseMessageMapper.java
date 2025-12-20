package io.github.johntortoise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.johntortoise.model.TortoiseMessage;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface TortoiseMessageMapper extends BaseMapper<TortoiseMessage> {

}
