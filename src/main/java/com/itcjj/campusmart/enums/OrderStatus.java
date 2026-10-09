package com.itcjj.campusmart.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态
 *
 * 0待付款 → 1已付款 → 2已发货 → 3已完成
 * 取消走 4
 *
 * ⚠️ 状态流转不是任意两条边都通的，合法的只有 8 条（见 docs/er-diagram.md）
 */
@Getter
@AllArgsConstructor          // 枚举的构造器由 Lombok 生成 private，安全
public enum OrderStatus {

    PENDING_PAYMENT(0, "待付款"),
    PAID(1, "已付款"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    /**
     * 按 code 反查枚举
     * 从数据库读出来的数字（比如 1）→ 变成 OrderStatus
     * 找不到返回 null，调用方自己决定怎么处理
     */
    public static OrderStatus fromCode(int code) {
        for (OrderStatus s : values()) {          // values() 是枚举自带的
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }
}
