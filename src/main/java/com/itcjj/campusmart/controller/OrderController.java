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
    @PutMapping("/pay/{orderId}")
    public Result<String> pay(@PathVariable Long orderId) {
        orderService.pay(orderId);
        return Result.success("支付成功");
    }
    @PutMapping("/ship/{orderId}")
    public Result<String> ship(@PathVariable Long orderId) {
        orderService.ship(orderId);
        return Result.success("发货成功");
    }
    @PutMapping("/confirm/{orderId}")
    public Result<String> confirm(@PathVariable Long orderId) {
        orderService.confirm(orderId);
        return Result.success("确认收货成功");
    }
    @PutMapping("/cancel/{orderId}")
    public Result<String> cancel(@PathVariable Long orderId) {
        orderService.cancel(orderId);
        return Result.success("取消订单成功");
    }
}
