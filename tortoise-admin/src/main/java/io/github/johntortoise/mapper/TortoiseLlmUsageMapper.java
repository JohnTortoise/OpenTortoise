package io.github.johntortoise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.johntortoise.model.TortoiseLlmUsage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface TortoiseLlmUsageMapper extends BaseMapper<TortoiseLlmUsage> {
    
    
    List<TortoiseLlmUsage> selectByUserId(Long userId);
    
    
    List<TortoiseLlmUsage> selectByConversationId(String conversationId);
    
    
    List<Map<String, Object>> selectUsageStatisticsByUserId(Long userId);
}