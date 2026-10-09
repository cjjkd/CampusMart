package com.itcjj.campusmart.controller;

import com.itcjj.campusmart.common.Result;
import com.itcjj.campusmart.dto.OrderCreateDTO;
import com.itcjj.campusmart.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping("/create")
    public Result<Long> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        Long orderId = orderService.createOrder(orderCreateDTO);
        return Result.success(orderId);
    }
}
