package com.itcjj.campusmart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite")                      // ⚠️ 不写，MP 推出来的表名不一定对
public class Favorite {
    @TableId(type = IdType.AUTO)            // ⚠️ 不写会走雪花算法（19 位 id）
    private Long id;

    private Long userId;
    private Long productId;

    @TableField(fill = FieldFill.INSERT)                 // 表里有这两列，要映射上
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
