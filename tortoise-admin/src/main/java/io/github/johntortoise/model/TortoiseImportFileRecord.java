package io.github.johntortoise.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TortoiseImportFileRecord {
    

    @TableId(type = IdType.AUTO)
    private Long id;


    private String importName;



    private Integer status;


    private String errorInfo;


    private Long createUserId;




    private Integer isDeleted;
    

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;



}