package com.itcjj.campusmart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")            // ⚠️ 必须写！类名 Order 推出来的表名是「保留字」order
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String  orderNo;
    private Long    buyerId;
    private Long    sellerId;
    private Long    productId;

    // 下单时的商品快照
    private String  productTitle;
    private String  productImages;

    private BigDecimal amount;
    private Integer    status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer deleted;
}
