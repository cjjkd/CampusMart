package com.itcjj.campusmart.service.impl;
import com.itcjj.campusmart.enums.OrderStatus;
import org.springframework.transaction.annotation.Transactional;
import com.itcjj.campusmart.common.CodeEnum;
import com.itcjj.campusmart.dto.OrderCreateDTO;
import com.itcjj.campusmart.entity.Order;
import com.itcjj.campusmart.exception.BizException;
import com.itcjj.campusmart.mapper.OrderMapper;
import com.itcjj.campusmart.mapper.ProductMapper;
import com.itcjj.campusmart.service.OrderService;
import com.itcjj.campusmart.entity.Product;
import com.itcjj.campusmart.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Slf4j
@Service


public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public Long createOrder(OrderCreateDTO orderCreateDTO) {
        //        ① 查商品  selectById(productId)
        //     ├─ null          → 报错「商品不存在」（2001）
        //     └─ status != 1   → 报错「商品已售出或已下架」
        Long productId = orderCreateDTO.getProductId();
        Product product = productMapper.selectById(productId);
        if (product == null) {
            log.warn("商品 {} 不存在", productId);
            throw new BizException(CodeEnum.PRODUCT_NOT_FOUND);
        }
        if (product.getStatus() != 1) {
            log.warn("商品 {} 状态异常 {}", productId, product.getStatus());
            throw new BizException(CodeEnum.PRODUCT_STATUS_ERROR);
        }
        //        ② 校验不是自己买自己的
        //     └─ product.sellerId == 当前登录用户id  → 报错
        if (product.getSellerId().equals(UserContext.get().getId())) {
            log.warn("用户 {} 尝试购买自己的商品 {}", UserContext.get().getId(), productId);
            throw new BizException(CodeEnum.CANNOT_BUY_OWN_PRODUCT);
        }
        //        ③ 扣减：把商品 status 从 1在售 改成 2已售出
        //     └─ 看影响行数
        product.setStatus(2);
        int affectedRows = productMapper.updateById(product);
        if (affectedRows == 0) {
            log.warn("商品 {} 扣减状态失败", productId);
            throw new BizException(CodeEnum.PRODUCT_STATUS_ERROR);
        }
        //        ④ 生成订单，insert 一条 orders
        //     ├─ order_no       ← 订单号（要唯一，见下方）
        //     ├─ buyer_id       ← 当前登录用户
        //     ├─ seller_id      ← product.sellerId
        //     ├─ product_id
        //     ├─ product_title / product_images / amount   ← 【快照】从商品带过来
        //     └─ status = 0 待付款
        Order order = new Order();
        // 时间戳 + 4 位随机数，例如 "202610091430127358"
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

        order.setOrderNo(orderNo);
        order.setBuyerId(UserContext.get().getId());
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setProductTitle(product.getTitle());      // 快照
        order.setProductImages(product.getImages());    // 快照
        order.setAmount(product.getPrice());            // 快照
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        orderMapper.insert(order);
        log.info("订单创建成功，买家{}，卖家{}，订单号：{}", order.getBuyerId(), order.getSellerId(), order.getOrderNo());
        return order.getId();                           // insert 后 MP 会把自增 id 回填进来
    }
}
