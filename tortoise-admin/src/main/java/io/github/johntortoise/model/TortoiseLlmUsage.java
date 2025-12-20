package io.github.johntortoise.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.johntortoise.dto.TortoiseLlmUsageDTO;
import io.github.johntortoise.enums.TortoiseLlmUsageEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TortoiseLlmUsage {
    

    @TableId(type = IdType.AUTO)
    private Long id;
    

    private String conversationId;
    

    private Long configId;


    private Integer type;


    private Integer inputTokens;
    

    private Integer outputTokens;
    

    private Integer totalTokens;
    

    private BigDecimal inputCost;
    

    private BigDecimal outputCost;
    

    private BigDecimal totalCost;
    

    private String requestContent;
    

    private String responseContent;
    

    private Integer isDeleted;
    

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    public static TortoiseLlmUsageDTO covertToDTO(TortoiseLlmUsage tortoiseLlmUsage,
                                                  Map<Long, String> modelIdToModelNameMap){

        TortoiseLlmUsageDTO build = TortoiseLlmUsageDTO.builder()
                .id(tortoiseLlmUsage.getId())
                .modelName(modelIdToModelNameMap.get(tortoiseLlmUsage.getConfigId()))
                .outputCost(tortoiseLlmUsage.getOutputCost())
                .outputTokens(tortoiseLlmUsage.getOutputTokens())
                .inputCost(tortoiseLlmUsage.getInputCost())
                .inputTokens(tortoiseLlmUsage.getInputTokens())
                .totalCost(tortoiseLlmUsage.getTotalCost())
                .totalTokens(tortoiseLlmUsage.getTotalTokens())
                .createTime(tortoiseLlmUsage.getCreateTime())
                .build();

        TortoiseLlmUsageEnum tortoiseLlmUsageEnum = TortoiseLlmUsageEnum.getById(tortoiseLlmUsage.getType());
        if(Objects.nonNull(tortoiseLlmUsageEnum)){
            build.setConsumeDesc(tortoiseLlmUsageEnum.getName());
        }
        return build;
    }
}