package io.github.johntortoise.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TortoiseLlmConfig {


    @TableId(type = IdType.AUTO)
    private Long id;



    private Long createUserId;


    @NotBlank(message = "配置名称不能为空")
    private String configName;


    @NotBlank(message = "大模型名称不能为空")
    private String modelName;


    @NotBlank(message = "API密钥不能为空")
    private String apiKey;


    @NotBlank(message = "API URL不能为空")
    private String apiUrl;


    @NotNull(message = "输入单元价格不能为空")
    private BigDecimal inputUnitPrice;


    @NotNull(message = "输出单元价格不能为空")
    private BigDecimal outputUnitPrice;


    @Builder.Default
    private BigDecimal cachePrice = BigDecimal.ZERO;


    @Builder.Default
    private Integer status = 1;


    @NotBlank(message = "配置描述不能为空")
    private String description;


    private BigDecimal temperature;


    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    @Builder.Default
    private Integer isDeleted = 0;
}