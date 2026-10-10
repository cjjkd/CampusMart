package com.itcjj.campusmart.service.impl;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itcjj.campusmart.enums.OrderStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service


public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    // ========== 幂等 Token 相关常量 ==========
    /** key 前缀 —— 发牌和撕票都用它，保证两边拼出来的 key 一定一致 */
    private static final String TOKEN_PREFIX = "order:token:";
    /** 凭证有效期：太短用户还没点完就过期，太长会积一堆废 key */
    private static final Duration TOKEN_TTL = Duration.ofMinutes(5);
    /** 存进 Redis 的占位值 —— StringRedisTemplate 存不了 null，我们只关心 key 在不在 */
    private static final String TOKEN_VALUE = "1";

    @Override
    public String generateToken() {
        // 生成长度 32 的随机串（去掉 UUID 里的横线），别人猜不到
        String token = UUID.randomUUID().toString().replace("-", "");
        // 存 Redis，5 分钟后自动消失
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, TOKEN_VALUE, TOKEN_TTL);
        log.info("已签发下单凭证 -> token={}", token);
        return token;
    }

    @Override
    @Transactional
    public Long createOrder(OrderCreateDTO orderCreateDTO) {
        // ---------- 第 0 步：撕票（幂等校验，必须放最前面） ----------
        // delete 是「检查 + 删除」一步完成的原子操作：
        //   返回 true  → 票还在，我是第一个用它的人 → 放行
        //   返回 false → 票早被撕过了（重复提交）→ 拒绝
        Boolean ok = redisTemplate.delete(TOKEN_PREFIX + orderCreateDTO.getToken());
        if (ok == null || !ok) {
            log.warn("下单凭证无效或已被使用 -> token={}", orderCreateDTO.getToken());
            throw new BizException(CodeEnum.DUPLICATE_SUBMIT);
        }

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

    @Override
    public void pay(Long orderId) {
        // ① 查订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(CodeEnum.ORDER_NOT_FOUND);
        }
        // ② 校验身份：只有买家能支付
        if (!order.getBuyerId().equals(UserContext.get().getId())) {
            throw new BizException(CodeEnum.NO_PERMISSION);
        }
        // ③ 校验起点状态
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new BizException(CodeEnum.ORDER_STATUS_ERROR);
        }
        // ④ 改状态
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, OrderStatus.PENDING_PAYMENT.getCode())   // ← 状态再写一遍
                .set(Order::getStatus, OrderStatus.PAID.getCode()));
        if (rows == 0) {
            throw new BizException(CodeEnum.ORDER_STATUS_ERROR);
        }
        log.info("订单支付成功 -> orderId={}, buyerId={}", orderId, order.getBuyerId());
    }

    @Override
    public void ship(Long orderId) {
        // ① 查订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(CodeEnum.ORDER_NOT_FOUND);
        }
        // ② 校验身份：只有卖家能发货
        if (!order.getSellerId().equals(UserContext.get().getId())) {
            throw new BizException(CodeEnum.NO_PERMISSION);
        }
        // ③ 校验起点状态
        if (order.getStatus() != OrderStatus.PAID.getCode()) {
            throw new BizException(CodeEnum.ORDER_STATUS_ERROR);
        }
        // ④ 改状态
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, OrderStatus.PAID.getCode())   // ← 状态再写一遍
                .set(Order::getStatus, OrderStatus.SHIPPED.getCode()));
        if (rows == 0) {
            throw new BizException(CodeEnum.ORDER_STATUS_ERROR);
        }
        log.info("订单发货成功 -> orderId={}, sellerId={}", orderId, order.getSellerId());
    }

    @Override
    public void confirm(Long orderId) {
        // ① 查订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(CodeEnum.ORDER_NOT_FOUND);
        }
        // ② 校验身份：只有买家能确认收货
        if (!order.getBuyerId().equals(UserContext.get().getId())) {
            throw new BizException(CodeEnum.NO_PERMISSION);
        }
        // ③ 校验起点状态
        if (order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            throw new BizException(CodeEnum.ORDER_STATUS_ERROR);
        }
        // ④ 改状态
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, OrderStatus.SHIPPED.getCode())   // ← 状态再写一遍
                .set(Order::getStatus, OrderStatus.COMPLETED.getCode()));
        if (rows == 0) {
            throw new BizException(CodeEnum.ORDER_STATUS_ERROR);
        }
        log.info("订单确认收货成功 -> orderId={}, buyerId={}", orderId, order.getBuyerId());
    }


    @Override
    @Transactional
    public void cancel(Long orderId) {
        // ① 查订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(CodeEnum.ORDER_NOT_FOUND);
        }
        // ② 校验身份：买家和卖家都能取消
        if (!order.getBuyerId().equals(UserContext.get().getId()) && !order.getSellerId().equals(UserContext.get().getId())) {
            throw new BizException(CodeEnum.NO_PERMISSION);
        }
        // ③ 校验起点状态
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode() && order.getStatus() != OrderStatus.PAID.getCode()) {
            throw new BizException(CodeEnum.ORDER_STATUS_ERROR);
        }
        int rows = 0;
        // ④ 改状态
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT.getCode()) {
            rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, orderId)
                    .eq(Order::getStatus, OrderStatus.PENDING_PAYMENT.getCode())   // ← 状态再写一遍
                    .set(Order::getStatus, OrderStatus.CANCELLED.getCode()));
        }
        else if (order.getStatus() == OrderStatus.PAID.getCode()) {
            rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, orderId)
                    .eq(Order::getStatus, OrderStatus.PAID.getCode())   // ← 状态再写一遍
                    .set(Order::getStatus, OrderStatus.CANCELLED.getCode()));
        }

        if (rows == 0) {
            throw new BizException(CodeEnum.ORDER_STATUS_ERROR);
        }
        // ⑤ 把商品放回「在售」
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, order.getProductId())
                .eq(Product::getStatus, 2)          // ⚠️ 只有「已售出」才回滚
                .set(Product::getStatus, 1));

        log.info("订单取消成功 -> orderId={}, buyerId={}", orderId, order.getBuyerId());
    }
}
