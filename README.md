# CampusMart · 校园二手交易平台

> 一个从 0 到 1 手写后端的学习型项目：不做花活，只把每个技术点真正落地一遍。

## 项目定位

面向校园场景的二手物品交易平台。核心链路：

**发布商品 → 浏览 / 搜索 → 收藏 → 下单 → 订单流转 → 评价**

学习目标：覆盖一个真实后端项目需要的完整能力栈——分层架构、ORM、认证鉴权、缓存、并发与事务、接口文档、部署。

## 技术栈

| 层次 | 选型 | 版本 |
|---|---|---|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 4.x |
| 持久层 | MyBatis-Plus | 3.5.17 |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis | 7.x（后续阶段） |
| 认证 | JWT（后续阶段） | — |
| 构建 | Maven | 3.9+ |
| 接口文档 | Knife4j / SpringDoc | 后续引入 |

## 分层结构约定

```
com.itcjj.campusmart
├── controller     接口层：参数校验、调用 service、返回统一响应
├── service        业务层：业务规则、事务边界
│   └── impl
├── mapper         持久层：MyBatis-Plus Mapper 接口
├── entity         数据库实体（与表一一对应）
├── dto            入参对象（Data Transfer Object）
├── vo             出参对象（View Object）
├── common         统一响应、错误码、常量
└── exception      自定义异常 + 全局异常处理器
```

约定：
- Controller **不写业务逻辑**，Service 只抛 `BizException`，异常统一由 `@RestControllerAdvice` 兜底
- 实体类不直接出入接口，入参用 dto、出参用 vo
- 统一响应体：`{ "code": 0, "msg": "success", "data": {...} }`

## 数据库设计

七张核心表：`user` / `category` / `product` / `order` / `order_item` / `favorite` / `review`

```mermaid
erDiagram
    USER ||--o{ PRODUCT : "发布"
    USER ||--o{ ORDERS : "下单"
    USER ||--o{ FAVORITE : "收藏"
    USER ||--o{ REVIEW : "评价"
    CATEGORY ||--o{ PRODUCT : "归类"
    PRODUCT ||--o{ ORDER_ITEM : "被购买"
    PRODUCT ||--o{ FAVORITE : "被收藏"
    PRODUCT ||--o{ REVIEW : "被评价"
    ORDERS ||--|{ ORDER_ITEM : "包含"
```

> - 完整字段定义（字段、类型、注释、索引规划）见 [`docs/er-diagram.md`](docs/er-diagram.md)
> - 注：`order` 是 MySQL 保留字，物理表名用 `orders`
> - 已完成建表：`user`（见 [`sql/init.sql`](sql/init.sql)）

## 已实现接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/hello` | 连通性测试 |
| GET | `/user/list` | 查询用户列表 |
| POST | `/user/add` | 新增用户 |
| PUT | `/user/update` | 修改用户（只更新传入的字段）|
| DELETE | `/user/delete/{id}` | 删除用户（逻辑删除，数据不真删）|

统一响应格式：`{"code": 0, "msg": "ok", "data": ...}`，`code = 0` 表示成功。

### 错误码分段规划

| 段位 | 用途 |
|---|---|
| `0` | 成功 |
| `4xx` | 参数错误 / 无权限 |
| `5xx` | 服务端异常 |
| `10xx` | user 模块 |
| `20xx` | product 模块 |
| `30xx` | order 模块 |

已定义错误码见 [`common/CodeEnum.java`](src/main/java/com/itcjj/campusmart/common/CodeEnum.java)：
`0` 成功 ｜ `1001` 用户名已存在 ｜ `1002` 用户不存在

> 业务异常与未预料的异常（空指针、数据库断开等）一律由
> [`GlobalExceptionHandler`](src/main/java/com/itcjj/campusmart/exception/GlobalExceptionHandler.java)
> 统一捕获并转换为上述格式，**Controller 层不写 try-catch**。

```jsonc
// POST /user/add —— 用户名重复时的实际返回
{"code": 1001, "msg": "用户名已存在", "data": null}
```

## 进度

### Phase 1 · Java 后端基础

- [x] **Week 00 · 起手**（9.10 ~ 9.13）
  - [x] Java 异常 / IO / Jackson JSON
  - [x] 集合框架（ArrayList vs LinkedList / HashMap 底层 / HashSet / 泛型擦除）
  - [x] 并发基础（线程创建与生命周期 / 竞态条件 / synchronized / volatile / 线程池）
  - [x] 仓库初始化 + README 骨架
  - [x] ER 图设计与评审（7 张表）
  - [x] `.gitignore` + Git 提交规范
  - [x] Spring Boot 工程骨架（4.1.1 + Java 17 + Maven）
  - [x] 接入 MySQL + MyBatis-Plus（逻辑删除、时间字段自动填充）
  - [x] **用户模块 CRUD 四个接口跑通**
- [ ] **Week 01 · 用户认证**（9.14 ~ 9.20）
  - 目标：分层规范 / 统一响应 / 全局异常处理 / JWT 注册登录 / 角色权限
  - [x] **Day 1 · 分层规范 + 统一响应 + 全局异常处理**
    - [x] `Result<T>` 统一响应体 + `CodeEnum` 错误码枚举
    - [x] `BizException` 业务异常 + `GlobalExceptionHandler` 全局兜底
    - [x] 用户名唯一性校验（Service 层抛异常，Controller 零 try-catch）
  - [ ] Day 2 · JWT 原理 + 注册 / 登录接口
  - [ ] Day 3 · 拦截器 + ThreadLocal 用户上下文
  - [ ] Day 4 · 参数校验 + 角色权限控制
  - [ ] Day 5 · BCrypt 密码加密 + 用户信息管理
  - [ ] Day 6 · Git 分支模型 + 用户模块自测
- [ ] Week 02 · 待补充

### Phase 2 · 业务开发

### Phase 3 · 进阶与部署

## 提交规范

采用 [Conventional Commits](https://www.conventionalcommits.org/)，格式：

```
<type>(<scope>): <subject>
```

| type | 含义 |
|---|---|
| feat | 新功能 |
| fix | 修 bug |
| docs | 文档 |
| refactor | 重构（不改行为） |
| test | 测试 |
| chore | 构建 / 依赖 / 配置 |
| style | 格式（不影响逻辑） |

示例：
```
feat(user): 新增用户注册接口
fix(order): 修复库存扣减并发下超卖问题
docs(readme): 补充 ER 图说明
chore: 升级 mybatis-plus 到 3.5.7
```
