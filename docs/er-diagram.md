# CampusMart · 数据库 ER 图

## 实体关系图

```mermaid
erDiagram
    USER ||--o{ PRODUCT   : "发布"
    USER ||--o{ ORDER     : "下单"
    USER ||--o{ FAVORITE  : "收藏"
    USER ||--o{ REVIEW    : "评价"
    CATEGORY ||--o{ PRODUCT : "归类"
    PRODUCT  ||--o{ ORDER_ITEM : "被购买"
    PRODUCT  ||--o{ FAVORITE   : "被收藏"
    PRODUCT  ||--o{ REVIEW     : "被评价"
    ORDER    ||--|{ ORDER_ITEM : "包含"

    USER {
        bigint   id          PK "用户ID"
        varchar  username       "用户名，唯一"
        varchar  password       "密码（BCrypt 加密）"
        varchar  nickname       "昵称"
        varchar  phone          "手机号"
        varchar  avatar         "头像URL"
        varchar  campus         "校区"
        tinyint  status         "状态 1正常 0禁用"
        datetime create_time    "创建时间"
        datetime update_time    "更新时间"
        tinyint  deleted        "逻辑删除 0未删 1已删"
    }

    CATEGORY {
        bigint   id          PK "分类ID"
        varchar  name           "分类名，唯一"
        int      sort           "排序值"
        tinyint  status         "状态 1启用 0停用"
        datetime create_time    "创建时间"
        datetime update_time    "更新时间"
    }

    PRODUCT {
        bigint   id          PK "商品ID"
        bigint   seller_id   FK "卖家ID -> user.id"
        bigint   category_id FK "分类ID -> category.id"
        varchar  title          "标题"
        text     description    "描述"
        decimal  price          "售价"
        decimal  original_price "原价"
        int      stock          "库存"
        tinyint  condition      "成色 1全新 2几乎全新 3轻微使用 4明显使用"
        varchar  cover_url      "封面图"
        tinyint  status         "状态 1在售 2已下架 3已售罄"
        int      view_count     "浏览量"
        datetime create_time    "创建时间"
        datetime update_time    "更新时间"
        tinyint  deleted        "逻辑删除"
    }

    ORDER {
        bigint   id          PK "订单ID"
        varchar  order_no       "订单号，唯一"
        bigint   buyer_id    FK "买家ID -> user.id"
        bigint   seller_id   FK "卖家ID -> user.id"
        decimal  total_amount   "订单总额"
        tinyint  status         "状态 1待付款 2待发货 3待收货 4已完成 5已取消"
        varchar  remark         "买家备注"
        datetime pay_time       "支付时间"
        datetime finish_time    "完成时间"
        datetime create_time    "创建时间"
        datetime update_time    "更新时间"
        tinyint  deleted        "逻辑删除"
    }

    ORDER_ITEM {
        bigint   id          PK "明细ID"
        bigint   order_id    FK "订单ID -> order.id"
        bigint   product_id  FK "商品ID -> product.id"
        varchar  product_title  "下单时商品标题快照"
        decimal  price          "下单时单价快照"
        int      quantity       "数量"
        decimal  subtotal       "小计 = price * quantity"
        datetime create_time    "创建时间"
    }

    FAVORITE {
        bigint   id          PK "收藏ID"
        bigint   user_id     FK "用户ID -> user.id"
        bigint   product_id  FK "商品ID -> product.id"
        datetime create_time    "创建时间"
    }

    REVIEW {
        bigint   id          PK "评价ID"
        bigint   order_id    FK "订单ID -> order.id"
        bigint   product_id  FK "商品ID -> product.id"
        bigint   user_id     FK "评价人ID -> user.id"
        tinyint  rating         "评分 1-5"
        varchar  content        "评价内容"
        datetime create_time    "创建时间"
    }
```

## 设计说明

| 设计点 | 决策 | 理由 |
|---|---|---|
| 逻辑删除 | `deleted` 字段（MyBatis-Plus `@TableLogic`） | 二手交易有纠纷追溯需求，不物理删 |
| 时间字段 | `create_time` / `update_time` 自动填充 | MP `MetaObjectHandler` 统一处理，不靠手写 |
| 金额类型 | `decimal(10,2)` | **绝不用 float/double**，精度丢失 |
| 订单明细 | 存商品标题与单价**快照** | 商品改价/改名后，历史订单金额不可变 |
| 库存 | 放在 `product.stock`，后续用乐观锁/Redis 防超卖 | 第 3 天先做基础 CRUD，并发在后续周处理 |
| 索引规划 | `user.username` 唯一索引；`product(seller_id)`、`product(category_id)`、`order(buyer_id)`、`favorite(user_id, product_id)` 联合唯一 | 按查询路径建索引 |

## 后续待办（写代码时再补）

- [ ] 是否拆出 `address`（收货地址）表
- [ ] 订单状态机：`1待付款 → 2待发货 → 3待收货 → 4已完成`，取消走 `5`
- [ ] 评价与订单的约束：一个订单项只能评价一次
