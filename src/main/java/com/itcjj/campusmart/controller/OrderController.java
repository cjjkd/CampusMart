package com.itcjj.campusmart.controller;

import com.itcjj.campusmart.common.Result;
import com.itcjj.campusmart.dto.OrderCreateDTO;
import com.itcjj.campusmart.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 创建订单（需带上 /order/token 拿到的幂等 Token）
    @PostMapping("/create")
    public Result<Long> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        Long orderId = orderService.createOrder(orderCreateDTO);
        return Result.success(orderId);
    }

    // 领取一次性下单凭证（幂等 Token）：前端点「购买」时先调这个，再带着它去下单
    @GetMapping("/token")
    public Result<String> getToken() {
        return Result.success(orderService.generateToken());
    }
}
