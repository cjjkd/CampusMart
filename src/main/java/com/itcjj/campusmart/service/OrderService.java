package com.itcjj.campusmart.service;

import com.itcjj.campusmart.dto.OrderCreateDTO;

public interface OrderService {
    Long createOrder(OrderCreateDTO orderCreateDTO);
}
