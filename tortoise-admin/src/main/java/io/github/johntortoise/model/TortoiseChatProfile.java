package io.github.johntortoise.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TortoiseChatProfile {


    @TableId(type = IdType.AUTO)
    private Long id;


    @NotBlank(message = "配置名称不能为空")
    private String name;


    @NotNull(message = "模型配置ID不能为空")
    private Long llmConfigId;


    @NotNull(message = "记忆策略ID不能为空")
    private Long memoryPolicyId;


    private String sk;



    private Long createUserId;

    @NotNull(message = "每个会话允许的单日token消耗数")
    private Long dailyTokenLimitConversation;

    @NotNull(message = "每个聊天配置允许的单日token消耗数")
    private Long dailyTokenLimitTotal;



    @Builder.Default
    private Integer status = 1;


    @Builder.Default
    private Integer isDeleted = 0;


    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}