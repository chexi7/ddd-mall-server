# DDD 架构落地指南

> 基于 [码如云 DDD 系列文章](https://docs.mryqr.com/ddd-introduction/)、[cap4j 框架](https://github.com/netcorepal/cap4j) 抽象思想，以及本 mall 商城项目的实践总结。
> 目标：作为团队技术业务落地的根本参考。

---

## 目录

1. [DDD 是什么](#1-ddd-是什么)
2. [为什么不是 CRUD](#2-为什么不是-crud)
3. [战略设计](#3-战略设计)
4. [项目结构](#4-项目结构)
5. [请求处理流程](#5-请求处理流程)
6. [聚合根与仓储](#6-聚合根与仓储)
7. [实体与值对象](#7-实体与值对象)
8. [应用服务与领域服务](#8-应用服务与领域服务)
9. [领域事件](#9-领域事件)
10. [CQRS](#10-cqrs)
11. [整洁架构与依赖倒置](#11-整洁架构与依赖倒置)
12. [Lombok 在 DDD 中的正确姿势](#12-lombok-在-ddd-中的正确姿势)
13. [常见问题 FAQ](#13-常见问题-faq)

---

## 1. DDD 是什么

**DDD（领域驱动设计）不是框架，不是架构模式，而是一种用业务语言驱动软件设计的方法论。** 本质上，DDD 是高级面向对象编程——让代码结构映射业务结构，让代码语言映射业务语言。

### 核心观点

- **业务逻辑住在领域对象里**，不是住在 Service 里
- **代码即文档**：看代码就能理解业务规则
- **技术为业务服务**：数据库、消息队列是实现细节，不是设计中心

### DDD 分为两部分

| 部分 | 关注点 | 产出 |
|------|--------|------|
| **战略设计** | 宏观：如何划分系统边界 | 限界上下文、统一语言 |
| **战术设计** | 微观：如何写代码 | 聚合根、实体、值对象、领域事件、仓储 |

> 参考：[DDD入门](https://docs.mryqr.com/ddd-introduction/) | [DDD概念大白话](https://docs.mryqr.com/ddd-in-plain-words/)

---

## 2. 为什么不是 CRUD

### 贫血模型（传统 CRUD）

```java
// Service 承载所有业务逻辑，对象只是数据容器
public class OrderService {
    public void payOrder(Long orderId) {
        OrderPO order = orderMapper.selectById(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("状态不允许");
        }
        order.setStatus("PAID");           // 直接改字段
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }
}
```

**问题**：业务规则散落在各个 Service 中，同一个实体的规则可能在 10 个 Service 里重复；对象没有行为，只有 getter/setter。

### 充血模型（DDD）

```java
// 业务逻辑在聚合根中，Service 只做编排
public class Order extends AggregateRoot {
    public void pay() {
        if (this.status != OrderStatus.PENDING_PAYMENT)
            throw new DomainException("只有待支付的订单才能支付");
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
        registerEvent(new OrderPaidEvent(this));
    }
}

// 应用服务（薄层，只做编排）—— 一个聚合一个 ApplicationService，方法名即业务用例
public class OrderApplicationService {
    @Transactional
    public void payOrder(PayOrderCommand command) {
        Order order = orderRepository.findByOrderNo(command.getOrderNo())
                .orElseThrow(() -> new DomainException("订单不存在"));
        order.pay();                       // 业务逻辑在聚合根
        orderRepository.save(order);       // 持久化 + 发布事件
    }
}
```

**优势**：业务规则集中在聚合根中，任何人改订单支付逻辑只需看 `Order.pay()`。`OrderApplicationService.payOrder` 这种"聚合 + 用例"的命名让调用方一眼看出业务意图。

> 参考：[后端不是CRUD](https://docs.mryqr.com/backend-is-not-crud/)

---

## 3. 战略设计

### 3.1 统一语言（Ubiquitous Language）

开发、产品、测试使用**同一套术语**，且这套术语直接体现在代码中。

| 业务说法 | 代码体现 | 反例 |
|----------|----------|------|
| "订单" | `Order` | `OrderInfo`, `OrderVO`, `TbOrder` |
| "下单" | `new Order(...)` | `orderService.createOrder()` |
| "支付" | `order.pay()` | `orderService.updateStatus("PAID")` |
| "库存预扣" | `inventory.lock(quantity)` | `inventoryMapper.decrease(...)` |

### 3.2 限界上下文（Bounded Context）

一个限界上下文 = 一套统一语言的边界。在本项目中，用**包结构**划分限界上下文：

```
domain/
├── order/       ← 订单上下文
├── product/     ← 商品上下文
├── inventory/   ← 库存上下文
├── cart/        ← 购物车上下文
└── member/      ← 会员上下文
```

**关键原则：上下文之间不直接引用聚合根对象，只通过 ID 关联 + 领域事件通信。**

> 参考：[战略设计](https://docs.mryqr.com/ddd-strategic-design/)

---

## 4. 项目结构

### 4.1 四层架构

```
┌─────────────────────────────────────┐
│          mall-web (接口层)            │  ← Controller、Request/Response DTO
├─────────────────────────────────────┤
│      mall-application (应用层)        │  ← ApplicationService、QueryService、事件处理器
├─────────────────────────────────────┤
│        mall-domain (领域层)           │  ← 聚合根、实体、值对象、领域事件、仓储接口
├─────────────────────────────────────┤
│    mall-infrastructure (基础设施层)    │  ← JPA 实现、DO 类、Converter、外部服务
└─────────────────────────────────────┘
```

### 4.2 依赖方向（**由外向内，内层不依赖外层**）

```
web → application → domain ← infrastructure
```

- `domain` 是最内层，**零框架依赖**（纯 Java + Lombok）
- `infrastructure` 依赖 `domain`（实现仓储接口），但 `domain` 不知道 `infrastructure` 的存在

### 4.3 完整包结构

```
mall/
├── mall-domain/                          # 领域层（纯 POJO）
│   └── domain/
│       ├── shared/                       # 共享内核
│       │   ├── AggregateRoot.java        #   聚合根基类
│       │   ├── Entity.java               #   实体基类
│       │   ├── ValueObject.java          #   值对象基类
│       │   ├── DomainEvent.java          #   领域事件接口
│       │   ├── DomainException.java      #   领域异常
│       │   └── Money.java                #   通用值对象
│       ├── order/                        # 按聚合分包
│       │   ├── Order.java                #   聚合根
│       │   ├── OrderItem.java            #   实体
│       │   ├── OrderStatus.java          #   枚举
│       │   ├── ShippingAddress.java       #   值对象
│       │   ├── OrderRepository.java      #   仓储接口
│       │   └── event/                    #   领域事件
│       │       ├── OrderCreatedEvent.java
│       │       ├── OrderPaidEvent.java
│       │       └── OrderCancelledEvent.java
│       ├── product/                      # 商品聚合
│       ├── inventory/                    # 库存聚合
│       ├── cart/                         # 购物车聚合
│       └── member/                       # 会员聚合
│
├── mall-application/                     # 应用层
│   └── application/
│       ├── command/                      # 写操作（按聚合扁平分包，一个聚合一个 ApplicationService）
│       │   ├── product/
│       │   │   ├── ProductApplicationService.java   #   聚合服务：createProduct / publishProduct / changePrice
│       │   │   ├── CreateProductCommand.java        #   Command 类（入参，纯数据载体）
│       │   │   ├── ChangePriceCommand.java
│       │   │   └── PublishProductCommand.java
│       │   ├── order/
│       │   │   ├── OrderApplicationService.java     #   聚合服务：createOrder / payOrder / cancelOrder
│       │   │   ├── CreateOrderCommand.java
│       │   │   ├── PayOrderCommand.java
│       │   │   └── CancelOrderCommand.java
│       │   └── auth/
│       │       ├── AuthApplicationService.java      #   认证服务：adminLogin / memberLogin
│       │       ├── AdminLoginCommand.java
│       │       ├── AdminLoginResult.java            #   有返回值的用例就近放 *Result
│       │       ├── MemberLoginCommand.java
│       │       └── MemberLoginResult.java
│       ├── query/                        # 读操作（CQRS）—— 同样一个聚合一个 QueryService
│       │   └── order/
│       │       ├── OrderQueryService.java           #   orderList / orderDetail
│       │       └── dto/OrderDetailDto.java
│       └── eventhandler/                 # 领域事件处理器（保留 *EventHandler 命名）
│           └── order/
│               ├── OrderCreatedEventHandler.java
│               └── OrderPaidEventHandler.java
│
├── mall-infrastructure/                  # 基础设施层
│   └── infrastructure/persistence/
│       ├── dataobject/                   # JPA DO 类（带注解）
│       │   ├── OrderDO.java
│       │   └── ProductDO.java
│       ├── converter/                    # DO ↔ 领域对象转换
│       │   ├── OrderConverter.java
│       │   └── ProductConverter.java
│       ├── impl/                         # 仓储实现
│       │   ├── OrderRepositoryImpl.java
│       │   └── ProductRepositoryImpl.java
│       ├── OrderJpaRepository.java       # Spring Data JPA 接口
│       └── ProductJpaRepository.java
│
└── mall-web/                             # 接口层
    └── web/
        ├── controller/
        │   ├── product/ProductController.java
        │   └── order/OrderController.java
        ├── request/
        │   ├── product/CreateProductRequest.java
        │   └── order/CreateOrderRequest.java
        └── response/ApiResponse.java
```

### 4.4 分包原则

| 原则 | 说明 |
|------|------|
| **领域层按聚合分包** | `domain/order/`、`domain/product/`，而非 `domain/entity/`、`domain/vo/` |
| **应用层按聚合扁平分包** | `command/order/`、`query/order/`、`eventhandler/order/`；同聚合的 `*ApplicationService` / `*Command` / `*Result` 平铺在一起，不再嵌套 `cmd/`、`handler/`、`result/` |
| **接口层按聚合分包** | `controller/order/`、`request/order/` |
| **基础设施层按技术关注点分包** | `dataobject/`、`converter/`、`impl/` |
| **所有 ApplicationService 方法必须接受 Command 对象** | 例如 `payOrder(PayOrderCommand command)` 而非 `payOrder(String orderNo)`，保持入参类型安全与可扩展性 |

应用层命名约定：

- **一个聚合一个 `*ApplicationService`，方法名即业务用例**（mryqr 推荐）。例：`OrderApplicationService` 暴露 `createOrder` / `payOrder` / `cancelOrder`，由 `MenuApplicationService.createMenu(cmd)` 直接读出业务意图。Controller 不再需要为"创建/支付/取消"各注入一个 `Create*ApplicationService`。
- 命令入参使用 `*Command`（无业务含义的纯数据载体），有业务返回的用例使用 `*Result` 作为出参，与对应的 `*ApplicationService` 同包同层放置。
- 查询同样按聚合归并，单聚合一个 `*QueryService`，方法名即查询用例（如 `MenuQueryService.menuTree()`、`OrderQueryService.orderList(...)`、`OrderQueryService.orderDetail(...)`）；查询入参少时直接传原始参数，多字段时升级为 `*Query` 类（详见 4.4.4），与服务同包平铺；查询读模型继续使用 `*Dto`，集中在 `dto/` 子包。
- Controller 等 Web 层协议对象使用 `*Request` / `ApiResponse` / `PageResponse`。
- `Handler` 后缀**仅保留给领域事件处理器**（`*EventHandler`）、框架拦截器等事件 / 技术适配场景，不再用于命令用例类。

#### 4.4.1 为什么 `*Command` 后缀**不**冗余

扁平化时我们删掉了 `cmd/`、`handler/`、`result/` 三个子包，但**类名后缀** `*Command` / `*Result` 必须保留。两者维度不同：

| 维度 | 含义 | 例子 |
|---|---|---|
| 包路径 `command/admin/` | "管理员聚合"下的"写操作"分组（CQRS 分类 + 限界上下文） | `application/command/admin/` |
| 类名后缀 `*Command` | 这是一个"命令对象"——一次用例的入参意图，不可变、纯数据载体 | `AssignPermissionsCommand` |

`Command` 是 CQRS 的通用名词（与 `Event`、`Query`、`DomainEvent` 同级），保留它有三个好处：

1. **在扁平包里区分角色**：同一个 `command/admin/` 下放着 `RoleApplicationService`（服务）、`AssignPermissionsCommand`（入参），靠后缀一眼识别。
2. **名词 vs 动词不再混乱**：类 `AssignPermissionsCommand` 是名词（"分配权限"这次动作的具象），方法 `roleApplicationService.assignPermissions(cmd)` 是动词（执行）。如果类名也叫 `AssignPermissions`，调用处变成 `service.assignPermissions(new AssignPermissions(...))`，主谓全丢。
3. **与领域术语谱系对齐**：`*Command`（未发生的意图）/ `*Event`（已发生的事实）/ `*Dto`（读模型）/ `*Result`（用例结果），形成稳定的业务名词体系。

> **小结**：`cmd/` 子包冗余是因为它在**包路径维度**重复了 `command`；而 `*Command` 后缀是**类语义维度**的标识，两者不冲突。

#### 4.4.2 ApplicationService 有响应时用 `*Result`，不要用 `*Response`

我们的项目已经在 Controller 层占用了 `*Request` / `ApiResponse` / `PageResponse`，应用层若也叫 `*Response` 会出现两个问题：

- **概念错位**：`Response` 暗示"对一次 HTTP 请求的应答"，而应用层不依赖、也不应该知道 HTTP 的存在（整洁架构原则）。
- **歧义**：哪天 Controller 端再加一个 `LoginResponse` 做 JSON 包装，名字就会撞车。

因此应用层一律使用 `*Result`，与 `*Command` 形成对称，全栈术语分工如下：

| 层 | 入参 | 出参 |
|---|---|---|
| Web Controller | `*Request`（HTTP 请求体 DTO） | `ApiResponse<T>` / `PageResponse<T>` |
| 应用层命令侧 | `*Command` | `*Result` / 原始类型 / `void` |
| 应用层查询侧 | 原始参数 / `*Query`（多字段时，详见 4.4.4） | `*Dto`、`PageResult<*Dto>` |
| 领域层 | 聚合根方法的形参 | 聚合根 / 值对象 |
| 领域事件 | — | `*Event`（过去式名词） |

**`*Result` 命名规则**：用动词短语前缀，与对应 `*Command` 配对，与 `*ApplicationService` 同包平铺：

| Command | Result |
|---|---|
| `AdminLoginCommand` | `AdminLoginResult` |
| `MemberLoginCommand` | `MemberLoginResult` |

#### 4.4.3 何时**不**需要新建 `*Result` 类

不是每个用例都要造一个 `*Result`，按返回内容判定：

| 返回内容 | 做法 | 本项目对应例子 |
|---|---|---|
| `void`（命令成功即可） | 不建类 | `OrderApplicationService.payOrder(cmd)` / `cancelOrder(cmd)` |
| 单个原始类型（`Long` / `String` / `Boolean`） | 直接返回原始类型 | `MenuApplicationService.createMenu(cmd) → Long`（菜单 ID）；`OrderApplicationService.createOrder(cmd) → String`（订单号） |
| 多字段、有内聚业务含义 | 建 `*Result` 类 | `AuthApplicationService.adminLogin(cmd) → AdminLoginResult`（token + 用户 + 角色 + 权限） |
| 单字段但语义需要明确（很少见） | 也可建 `*Result`（值对象思路） | 如未来 `placeOrder → OrderPlacedResult { orderNo, totalAmount, expireAt }` |

**经验法则**：字段数 ≥ 2，或者将来有可能扩展字段就建 `*Result`；只返回一个 ID/订单号就直接返回基础类型，避免空壳类。

#### 4.4.4 查询侧入参用 `*Query`，**不要**用领域 Entity / `*Request` / `*Command`

命令侧用 `*Command`，对称地查询侧用 `*Query`，与 CQRS 的术语谱系保持一致。**绝不能**直接拿领域 Entity / AggregateRoot 当查询入参，几条红线如下：

| 反对做法 | 原因 |
|---|---|
| 用 `domain.Admin` 等聚合根作 query 入参 | 破坏 CQRS（查询条件 ≠ 领域实体）+ 破坏依赖方向（web 会被迫引用 domain）+ 干扰聚合不变量（构造校验对查询无意义）+ 语义歧义（不知道哪些字段是过滤条件） |
| 复用 web 层的 `*Request` | `*Request` 绑定 HTTP 协议语义，整洁架构里 application 不应依赖 web |
| 复用命令侧的 `*Command` | CQRS 的核心就是命令 / 查询术语严格分离 |
| 把入参塞进 `dto/` 子包 | `dto/` 已专门承载读模型**出参**，再混入入参职责会糊掉目录语义 |

**推荐做法**：与 `*Command` 完全对称。`*Query` 类与对应 `*QueryService` **同包平铺**：

```
application/query/
├── admin/
│   ├── AdminQueryService.java          ← 服务
│   ├── AdminListQuery.java             ← 多字段入参（按需新增）
│   ├── RoleQueryService.java
│   ├── MenuQueryService.java
│   └── dto/
│       ├── AdminListItemDto.java       ← 出参（读模型）
│       └── ...
└── order/
    ├── OrderQueryService.java
    ├── OrderListQuery.java             ← 字段变多时新增
    └── dto/
        ├── OrderListItemDto.java
        └── OrderDetailDto.java
```

`*Query` 类的内容形态可比 `*Command` 宽松一些（查询入参没有"业务事实"语义，可变性约束可以放宽），用 `@Getter @Setter` 通常即可：

```java
@Getter
@Setter
public class AdminListQuery {
    private int page = 1;
    private int size = 10;
    private String keyword;
    private Boolean enabled;
    private LocalDate createdFrom;
    private LocalDate createdTo;
}
```

**何时升级到 `*Query`，何时保留原始参数**（与 4.4.3 决策表对称）：

| 入参情况 | 做法 | 本项目对应 |
|---|---|---|
| 0 ~ 1 个原始类型参数 | 直接传 | `MenuQueryService.menuTree()`、`OrderQueryService.orderDetail(String orderNo)` |
| 2 ~ 3 个原始类型参数 | 直接传 | `RoleQueryService.roleList(int page, int size)`、`AdminQueryService.adminList(int page, int size, String keyword)` |
| ≥ 4 个参数 / 内聚业务含义 / 需要 Bean Validation / 字段还会扩展 | 建 `*Query` 类 | `OrderQueryService.orderList(int, int, String, String)` 已 4 个参数，再加任何筛选字段就该升级为 `OrderListQuery` |
| 多组互斥搜索条件（搜索 / 报表场景） | 建 `*Query` 类，可配 `@AssertTrue` 等校验 | 如未来 `ProductSearchQuery` |

> 参考：[项目结构](https://docs.mryqr.com/ddd-project-structure/) | [CQRS](https://docs.mryqr.com/ddd-cqrs/)

---

## 5. 请求处理流程

### 5.1 创建流程（新建聚合根）

```
Controller → ApplicationService → new AggregateRoot(...) → Repository.save()
```

```java
// OrderApplicationService.java —— 一个聚合一个服务，方法名即业务用例
@Transactional
public String createOrder(CreateOrderCommand command) {
    List<OrderItem> items = ...;           // 构建订单项
    ShippingAddress address = ...;          // 构建值对象

    Order order = new Order(orderNo, memberId, items, address);  // 业务规则在构造函数
    orderRepository.save(order);           // 持久化 + 发布 OrderCreatedEvent

    return orderNo;
}
```

### 5.2 更新流程（修改聚合根）

```
Controller → ApplicationService → Repository.findById() → AggregateRoot.doSomething() → Repository.save()
```

```java
// OrderApplicationService.java —— 同一个聚合的另一个用例方法
@Transactional
public void payOrder(PayOrderCommand command) {
    Order order = orderRepository.findByOrderNo(command.getOrderNo())  // 1. 取出聚合根
            .orElseThrow(() -> new DomainException("订单不存在"));
    order.pay();                                                // 2. 调用业务方法
    orderRepository.save(order);                                // 3. 保存 + 发布事件
}
```

### 5.3 查询流程（CQRS 读侧）

```
Controller → QueryService → 直接查数据库 → 返回 DTO
```

查询不走聚合根，直接查库返回 DTO，避免加载完整聚合的开销。

> 参考：[请求处理流程](https://docs.mryqr.com/ddd-request-process-flow/)

---

## 6. 聚合根与仓储

### 6.1 聚合根是什么

聚合根 = **一组紧密关联对象的统领者**。外部只能通过聚合根访问内部对象，聚合根负责维护内部一致性。

```
Order（聚合根）
├── OrderItem（实体，外部不能直接操作）
├── ShippingAddress（值对象）
└── OrderStatus（枚举）
```

### 6.2 聚合根设计原则

| 原则 | 说明 | 示例 |
|------|------|------|
| **业务方法代替 setter** | 不暴露 `setStatus()`，而是 `pay()`, `cancel()` | `order.pay()` 而非 `order.setStatus(PAID)` |
| **在构造函数中校验** | 创建时就保证合法 | `if (items.isEmpty()) throw ...` |
| **聚合间只通过 ID 引用** | Order 不持有 Product 对象，只持有 productId | `orderItem.productId` |
| **聚合间通过事件通信** | 下单后库存预扣，用 OrderCreatedEvent | 不在 `OrderApplicationService.createOrder` 里直接调 `InventoryRepository` |
| **尽量保持聚合小** | 聚合越大，并发冲突越多 | Order 不包含 Product 的完整信息 |

### 6.3 仓储模式

**核心思想：领域层定义接口，基础设施层实现。**

```java
// 领域层：只声明"我需要什么能力"
public interface OrderRepository {
    Optional<Order> findById(Long id);
    Optional<Order> findByOrderNo(String orderNo);
    void save(Order order);
}

// 基础设施层：决定"怎么实现"
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpaRepository;          // Spring Data JPA
    private final ApplicationEventPublisher eventPublisher;   // 事件发布

    @Override
    public void save(Order order) {
        OrderDO saved = jpaRepository.save(OrderConverter.toDO(order));  // 领域对象 → DO → 数据库
        order.setId(saved.getId());
        // 发布领域事件
        order.getDomainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();
    }
}
```

### 6.4 DO 转换（领域层纯净方案）

领域对象不带 JPA 注解，基础设施层通过 Converter 做双向转换：

```
保存：领域对象 → Converter.toDO() → DO(JPA Entity) → 数据库
查询：数据库 → DO(JPA Entity) → Converter.toDomain() → 领域对象
```

> 参考：[聚合根与资源库](https://docs.mryqr.com/ddd-aggregate-root-and-repository/)

---

## 7. 实体与值对象

### 7.1 区别

| 维度 | 实体（Entity） | 值对象（Value Object） |
|------|----------------|----------------------|
| **标识** | 有唯一 ID | 没有 ID，靠属性值区分 |
| **可变性** | 有生命周期，可变 | 不可变，修改就是创建新实例 |
| **相等性** | ID 相同就相等 | 所有属性相同就相等 |
| **举例** | `Order`, `OrderItem`, `Product` | `Money`, `Address`, `ShippingAddress` |

### 7.2 值对象设计原则

```java
// Money —— 所有字段 final，没有 setter，运算返回新实例
@Getter
@EqualsAndHashCode
public class Money {
    private final BigDecimal amount;

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));  // 返回新实例
    }
}
```

**值对象的好处**：
- 用 `Money` 替代 `BigDecimal`，让代码有业务含义
- `ShippingAddress` 替代 6 个松散的字符串字段
- 自带校验逻辑（构造函数中校验）

### 7.3 实体设计原则

- 有唯一 ID（通常数据库自增）
- 通过 ID 判断相等性（不是属性值）
- 可以有状态变更，但应该通过**有业务含义的方法**变更

> 参考：[实体与值对象](https://docs.mryqr.com/ddd-entity-and-value-object/)

---

## 8. 应用服务与领域服务

### 8.1 应用服务（Application Service）

**位置**：应用层
**职责**：编排，不含业务逻辑
**特征**：薄，像个指挥官——自己不干活，只协调

```java
// 标准三部曲：取出 → 调用 → 保存。方法名即业务用例，由 OrderApplicationService 持有
@Transactional
public void payOrder(PayOrderCommand command) {
    Order order = orderRepository.findByOrderNo(command.getOrderNo()).orElseThrow(...);
    order.pay();                    // 业务逻辑在聚合根
    orderRepository.save(order);    // 持久化
}
```

**应用服务可以做的事**：
- 开启事务
- 调用仓储加载/保存聚合根
- 调用聚合根的业务方法
- 协调多个聚合根（通过领域事件）

**应用服务不应该做的事**：
- 包含 if/else 业务判断
- 直接操作聚合根内部状态
- 包含计算逻辑

### 8.2 领域服务（Domain Service）

**位置**：领域层
**职责**：处理**不属于任何单个聚合根**的业务逻辑
**使用场景**：跨聚合的业务规则、需要外部信息的校验

```java
// 示例：定价服务（需要查多个聚合的信息来计算价格）
public class PricingService {
    public Money calculateOrderPrice(List<OrderItem> items, Member member) {
        Money subtotal = items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.zero(), Money::add);

        // 会员折扣（需要跨聚合信息）
        if (member.isVip()) {
            return subtotal.multiply(0.9);
        }
        return subtotal;
    }
}
```

### 8.3 判断逻辑放哪里

| 场景 | 放在哪里 | 原因 |
|------|----------|------|
| 订单能否支付 | `Order.pay()` | 只涉及订单自身状态 |
| 库存够不够 | `Inventory.lock()` | 只涉及库存自身 |
| 跨聚合的价格计算 | `PricingService` | 需要多个聚合的信息 |
| 调用外部支付接口 | 应用服务 | 技术编排 |

> 参考：[应用服务与领域服务](https://docs.mryqr.com/ddd-application-service-and-domain-service/)

---

## 9. 领域事件

### 9.1 什么是领域事件

领域事件 = **"某件业务上有意义的事情发生了"**。用过去式命名：`OrderCreated`, `OrderPaid`, `OrderCancelled`。

### 9.2 为什么需要领域事件

**解决跨聚合协作问题**。下单后需要扣库存、清购物车——如果在一个 ApplicationService 里直接调用三个仓储，聚合之间就耦合了。

```
❌ 耦合方式：OrderApplicationService.createOrder 里直接调用 InventoryRepository + CartRepository
✅ 事件方式：Order 发布 OrderCreatedEvent → EventHandler 分别处理库存和购物车
```

### 9.3 实现方式

**第一步：聚合根注册事件**

```java
public class Order extends AggregateRoot {
    public Order(...) {
        // ... 业务逻辑
        registerEvent(new OrderCreatedEvent(this));    // 注册到事件列表
    }
}
```

**第二步：仓储保存时发布事件**

```java
public class OrderRepositoryImpl implements OrderRepository {
    public void save(Order order) {
        jpaRepository.save(OrderConverter.toDO(order));
        order.getDomainEvents().forEach(eventPublisher::publishEvent);  // 发布
        order.clearDomainEvents();                                       // 清空
    }
}
```

**第三步：事件处理器响应事件**

```java
@Component
public class OrderCreatedEventHandler {
    @EventListener
    @Transactional
    public void handle(OrderCreatedEvent event) {
        // 库存预扣
        for (OrderCreatedEvent.OrderItemInfo item : event.getItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(...);
            inventory.lock(item.getQuantity());
            inventoryRepository.save(inventory);
        }
    }
}
```

### 9.4 事件类设计

```java
@Getter
public class OrderCreatedEvent implements DomainEvent {
    private final Long orderId;
    private final String orderNo;
    private final Long memberId;
    private final List<OrderItemInfo> items;    // 只携带 ID 和必要数据，不携带聚合根引用
    private final LocalDateTime occurredOn;
}
```

**注意：事件只携带 ID 和必要的数据快照，不携带聚合根对象的引用。**

> 参考：[领域事件](https://docs.mryqr.com/ddd-domain-events/)

---

## 10. CQRS

### 10.1 核心思想

**命令（写）和查询（读）走不同的路径。**

```
写（Command）：Controller → ApplicationService → AggregateRoot → Repository.save()
读（Query）  ：Controller → QueryService → 直接查数据库 → DTO
```

### 10.2 为什么查询不走聚合根

- 聚合根是为**维护业务规则**设计的，不是为**展示数据**设计的
- 查询需要跨聚合 JOIN、分页排序，聚合根做不了
- 查询走聚合根会加载大量不需要的关联对象

### 10.3 本项目的简化 CQRS

```java
// 查询服务：一个聚合一个 *QueryService，方法名即查询用例
@Service
public class OrderQueryService {
    @Transactional(readOnly = true)
    public OrderDetailDto orderDetail(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElseThrow(...);
        return toDto(order);   // 投影到 DTO
    }

    @Transactional(readOnly = true)
    public PageResult<OrderListItemDto> orderList(int page, int size, String status, String keyword) {
        // ...直接查库，绕过聚合根
    }
}
```

对于更复杂的查询场景（列表、分页、跨表 JOIN），可以直接用 JPA/JPQL/原生 SQL 查询，绕过仓储和聚合根。

> 参考：[CQRS](https://docs.mryqr.com/ddd-cqrs/)

---

## 11. 整洁架构与依赖倒置

### 11.1 核心原则

**领域层是核心，不依赖任何外部框架。** 基础设施层（JPA、Spring）是可替换的实现细节。

```
依赖方向：外层依赖内层，内层不知道外层的存在

  ┌──────────────────────┐
  │       web 层          │  依赖 application
  ├──────────────────────┤
  │   application 层      │  依赖 domain
  ├──────────────────────┤
  │     domain 层         │  ← 最内层，零依赖
  ├──────────────────────┤
  │  infrastructure 层    │  依赖 domain（实现接口）
  └──────────────────────┘
```

### 11.2 本项目的实现

| 层 | Maven 依赖 | 框架依赖 |
|---|---|---|
| `mall-domain` | 无（只有 Lombok） | **零框架依赖** |
| `mall-application` | → domain | Spring TX, Spring Context, SLF4J |
| `mall-infrastructure` | → domain | Spring Data JPA, Hibernate |
| `mall-web` | → application, infrastructure | Spring Boot Web |

### 11.3 依赖倒置实践

```java
// 领域层定义接口（不知道 JPA 的存在）
public interface OrderRepository {
    void save(Order order);
}

// 基础设施层实现接口（知道 JPA，也知道 domain）
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpaRepository;  // Spring Data
    // ...
}
```

### 11.4 DO 转换保证领域纯净

领域对象（`Order`）不带 `@Entity`、`@Table` 等 JPA 注解。JPA 注解放在 DO 类（`OrderDO`）上，通过 Converter 双向转换：

```
领域层：Order (纯 POJO)          ← 只有业务逻辑
基础设施层：OrderDO (@Entity)    ← 只有持久化映射
转换层：OrderConverter           ← toDomain() / toDO()
```

> 参考：[整洁架构能有多整洁](https://docs.mryqr.com/how-clean-can-clean-architecture-be/)

---

## 12. Lombok 在 DDD 中的正确姿势

### 12.1 推荐用法

| 注解 | 用在哪里 | 说明 |
|------|----------|------|
| `@Getter` | 聚合根、实体 | 读取属性是安全的 |
| `@Getter @EqualsAndHashCode` | 值对象 | 值对象靠属性值判等 |
| `@Getter @RequiredArgsConstructor` | Command、Event | 不可变数据载体 |
| `@Getter @Setter` | DTO、DO | 纯数据传输对象 |
| `@RequiredArgsConstructor` | ApplicationService、DomainService、EventHandler | 构造器注入 |
| `@Slf4j` | 事件处理器 | 日志 |

### 12.2 禁止用法

| 注解 | 禁止用在哪里 | 原因 |
|------|-------------|------|
| `@Setter` | 聚合根 | `order.setStatus(PAID)` 绕过了业务规则，应该用 `order.pay()` |
| `@Data` | 聚合根、实体 | 等于 @Getter + @Setter + @ToString + @EqualsAndHashCode，setter 会破坏封装 |
| `@Builder` | 聚合根 | 构造应通过有业务含义的构造函数或工厂方法 |
| `@NoArgsConstructor(PUBLIC)` | 聚合根 | 允许创建无效状态的对象 |

### 12.3 聚合根的 Setter 策略

聚合根的 setter 仅用于**仓储重建**（从数据库加载时设置字段）：

```java
@Getter                           // ✅ 所有字段可读
public class Order extends AggregateRoot {
    @Setter private OrderStatus status;   // 仅供 Converter 重建用
    // ...

    // ✅ 业务方法（有业务含义、有规则校验、有事件发布）
    public void pay() {
        if (this.status != OrderStatus.PENDING_PAYMENT)
            throw new DomainException("只有待支付的订单才能支付");
        this.status = OrderStatus.PAID;
        registerEvent(new OrderPaidEvent(this));
    }
}
```

**团队约定：业务代码中禁止调用聚合根的 setter，setter 仅供 infrastructure 层 Converter 使用。**

> 参考：[Lombok 的正确姿势](https://docs.mryqr.com/how-to-use-lombok-in-ddd/)

---

## 13. 常见问题 FAQ

### Q1: DDD 适合什么项目？

适合**业务逻辑复杂**的项目。如果只是简单的增删改查（管理后台、配置系统），用传统 MVC + CRUD 更高效。DDD 有一定的学习成本和代码量开销。

### Q2: 聚合根应该多大？

**尽量小**。一个聚合根包含的实体越多，并发冲突越大，加载越慢。本项目中 `Order` 包含 `OrderItem` 和 `ShippingAddress`，这是合理的——它们的生命周期完全依附于订单。但 `Product` 不在 `Order` 聚合内，因为它有独立的生命周期。

### Q3: 聚合之间怎么通信？

**通过领域事件**。`Order` 创建后发布 `OrderCreatedEvent`，`OrderCreatedEventHandler` 监听并操作 `Inventory` 聚合。不要在一个 ApplicationService 中同时操作多个聚合的仓储。

### Q4: 仓储接口应该有哪些方法？

**只定义业务需要的方法**，不要提供通用的 `findAll()`、`findByXxx()`。仓储接口是领域语言的一部分：

```java
// ✅ 好的仓储接口——方法名有业务含义
public interface ProductRepository {
    Optional<Product> findById(Long id);
    List<Product> findOnSaleProducts();    // 业务概念：在售商品
    void save(Product product);
}

// ❌ 不好的仓储接口——暴露了技术细节
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 暴露了所有 CRUD 方法
}
```

### Q5: DO 转换是不是太啰嗦了？

是有额外代码量，但换来的是：
- 领域层完全纯净，可以独立测试
- 更换 ORM 框架（JPA→MyBatis）只改基础设施层
- 领域对象结构可以和数据库表结构不同

如果团队觉得成本太高，可以折中：领域对象直接加 JPA 注解（实用方案），但要意识到这是在**领域纯净性和开发效率之间的权衡**。

### Q6: 领域事件是同步还是异步？

本项目使用 `@EventListener`（同步，同事务）。同步的好处是简单、一致性强；缺点是处理器失败会导致整个事务回滚。

如果需要异步或事务后处理，可以改为：
- `@TransactionalEventListener(phase = AFTER_COMMIT)` — 事务提交后异步处理
- 使用消息队列（RabbitMQ/RocketMQ）发布集成事件 — 跨服务通信

### Q7: cap4j 框架做了什么？

cap4j 的核心思想和本项目一致，但它提供了更多框架级封装：
- `Mediator` 中介者模式，统一调度 Factory、Repository、DomainService
- `UnitOfWork` 工作单元，统一管理事务和事件发布
- `Specification` 规格模式，可复用的业务规则校验
- 代码生成脚手架

本项目选择**不依赖 cap4j**，用 Spring 原生能力实现同样的 DDD 模式，降低学习成本。

> 参考：[DDD FAQ](https://docs.mryqr.com/ddd-faq/)

---

## 参考资料

- [码如云 DDD 系列（14 篇）](https://docs.mryqr.com/ddd-introduction/) — 最佳中文 DDD 实践文章
- [cap4j 框架](https://github.com/netcorepal/cap4j) — Java DDD 框架参考
- [cleanddd-skills](https://github.com/netcorepal/cleanddd-skills) — DDD 技能流程化方法
- 《实现领域驱动设计》— Vaughn Vernon（红皮书）
- 《领域驱动设计》— Eric Evans（蓝皮书）
