package com.itcjj.campusmart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单的入参
 *
 * ⚠️ 只有一个字段 —— 其余的订单信息全部由服务端自己算出来，不接受前端传
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
