package io.github.johntortoise.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TortoiseTokenTotalRecord {


    @TableId(type = IdType.AUTO)
    private Long id;



    private String conversationId;



    private Long chatProfileId;

    
    @NotNull(message = "token总数不能为空")
    private Long total;

    
    @Builder.Default
    private Integer isDeleted = 0;

    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}