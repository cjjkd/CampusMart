package com.itcjj.campusmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@Data
@TableName("user")
//对应user这张表

public class User {
    @TableId(type = IdType.AUTO)
    //标记主键
    private Long id;
    private String username;
    private String role;
    @JsonIgnore
    private String password;
    private String nickname;
    private String phone;
    private String avatar;
    private String campus;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    //字段自动填
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer deleted;
}
