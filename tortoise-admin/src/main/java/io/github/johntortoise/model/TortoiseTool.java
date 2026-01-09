package io.github.johntortoise.model;

import com.baomidou.mybatisplus.annotation.*;
import io.github.johntortoise.dto.TortoiseToolDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tortoise_tool")
public class TortoiseTool {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "工具名称不能为空")
    private String name;

    @TableField("`desc`")
    private String desc;

    private String fieldConfig;

    @NotBlank(message = "请求URL不能为空")
    private String url;

    @NotBlank(message = "请求方法不能为空")
    private String method;

    private String headers;

    @Builder.Default
    @TableField("is_deleted")
    private Integer isDeleted = 0;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public static TortoiseToolDTO covertToDTO(TortoiseTool tortoiseTool){
        return TortoiseToolDTO.builder().toolName(tortoiseTool.getName())
                .desc(tortoiseTool.getDesc())
                .id(tortoiseTool.getId())
                .build();
    }
}