package io.github.johntortoise.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tortoise_conversation_tool")
public class TortoiseConversationTool {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String toolId;

    private String conversationId;

    @Builder.Default
    @TableField("is_deleted")
    private Integer isDeleted = 0;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}









