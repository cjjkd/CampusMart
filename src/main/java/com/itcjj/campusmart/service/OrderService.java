package com.itcjj.campusmart.service;

import com.itcjj.campusmart.dto.OrderCreateDTO;

public interface OrderService {

    Long createOrder(OrderCreateDTO orderCreateDTO);

    /** 生成一次性下单凭证（幂等 Token）：存 Redis，有效期 5 分钟 */
    String generateToken();

    void pay(Long orderId);

    void ship(Long orderId);

    void confirm(Long orderId);

    void cancel(Long orderId);

}
