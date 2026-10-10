package com.itcjj.campusmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单的入参
 *
 * ⚠️ 只有两个字段 —— 其余订单信息全部由服务端算出来，不接受前端传
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 幂等 Token：先调 GET /order/token 领取，带上下单；同一张票只能用一次 */
    @NotBlank(message = "下单凭证不能为空")
    private String token;
}
