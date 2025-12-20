package io.github.johntortoise.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TortoiseMemoryPolicy {
    

    @TableId(type = IdType.AUTO)
    private Long id;


    private String name;


    private Long llmConfigId;


    private Long createUserId;


    private String config;


    private Integer isDeleted;
    

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    public static TortoiseMemoryPolicyDTO covertToDTO(TortoiseMemoryPolicy tortoiseMemoryPolicy, Map<Long, String> modelIdToModelNameMap){
        return TortoiseMemoryPolicyDTO.builder()
                .llmModelConfig(tortoiseMemoryPolicy.getLlmConfigId())
                .modelName(modelIdToModelNameMap.get(tortoiseMemoryPolicy.getLlmConfigId()))
                .name(tortoiseMemoryPolicy.getName())
                .id(tortoiseMemoryPolicy.getId())
                .build();

    }


}