package com.itcjj.campusmart.task;

import com.itcjj.campusmart.entity.Order;
import com.itcjj.campusmart.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderTimeoutTask {

    @Autowired
    private OrderService orderService;

    /** 每 30 秒扫一次（fixedDelay：上一次跑完再等 30 秒） */
    @Scheduled(fixedDelay = 30_000)
    public void closeTimeoutOrders() {
        List<Order> list = orderService.findTimeoutOrders();

        // 一条都没有 → 安静退出
        // ⚠️ 这里【不能】打日志 —— 任务每 30 秒跑一次，会刷爆日志
        if (list.isEmpty()) {
            return;
        }

        log.info("发现超时订单 -> 共 {} 条", list.size());

        for (Order order : list) {
            try {
                orderService.closeTimeoutOrder(order.getId());
            } catch (Exception e) {
                // 单条失败不影响其他条；这里只记日志
                log.error("关闭超时订单失败 -> orderId={}", order.getId(), e);
            }
        }
    }
}
