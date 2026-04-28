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
├── member/      ← 会员上下文
└── admin/       ← 管理上下文（Admin + Menu + Role + Permission）
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
│       │   ├── DomainEvent.java          #   领域事件接口
│       │   ├── DomainException.java      #   领域异常
│       │   ├── Money.java                #   通用值对象（@Getter @EqualsAndHashCode，final 字段）
│       │   └── CommonStatus.java         #   通用状态枚举（ENABLED / DISABLED）
│       ├── order/                        # 订单聚合（一个聚合根一个包）
│       │   ├── Order.java                #   聚合根（包顶层，一眼可见统领者）
│       │   ├── OrderItem.java            #   内部实体（包顶层，属于 Order 内部）
│       │   ├── ShippingAddress.java       #   内部值对象（包顶层，属于 Order 内部）
│       │   ├── OrderStatus.java          #   枚举（只有 1 个，放顶层）
│       │   ├── OrderRepository.java      #   仓储接口（命令侧，只保留写操作）
│       │   ├── query/                    #   CQRS 查询端口（读侧接口）
│       │   │   ├── OrderQueryPort.java   #   查询端口接口（基础设施层实现）
│       │   │   └── OrderPageResult.java  #   查询返回模型
│       │   └── event/                    #   领域事件子包
│       │       ├── OrderCreatedEvent.java
│       │       ├── OrderPaidEvent.java
│       │       └── OrderCancelledEvent.java
│       ├── product/                      # 商品聚合
│       │   ├── Product.java              #   聚合根
│       │   ├── ProductSku.java           #   内部实体
│       │   ├── ProductStatus.java        #   枚举
│       │   ├── ProductRepository.java    #   仓储接口（命令侧）
│       │   ├── query/                    #   CQRS 查询端口
│       │   │   ├── ProductQueryPort.java
│       │   │   └── ProductPageResult.java
│       │   └── event/
│       │       ├── ProductCreatedEvent.java
│       │       ├── ProductPriceChangedEvent.java
│       ├── inventory/                    # 库存聚合
│       │   ├── Inventory.java            #   聚合根
│       │   ├── InventoryRepository.java  #   仓储接口
│       │   └── event/
│       │       ├── InventoryDeductedEvent.java
│       │       ├── InventoryRestoredEvent.java
│       ├── member/                       # 会员聚合
│       │   ├── Member.java               #   聚合根
│       │   ├── Address.java              #   内部值对象
│       │   ├── MemberRepository.java     #   仓储接口（命令侧）
│       │   └── query/
│       │       └── MemberQueryPort.java
│       ├── cart/                         # 贑物车聚合
│       │   ├── Cart.java                 #   聚合根
│       │   ├── CartItem.java             #   内部实体（使用 Money 值对象）
│       │   └── CartRepository.java       #   仓储接口
│       ├── admin/                        # 管理员聚合（独立包）
│       │   ├── Admin.java                #   聚合根
│       │   └── AdminRepository.java      #   仓储接口
│       ├── role/                         # 角色聚合（独立包——从 admin 拆出）
│       │   ├── Role.java                 #   聚合根
│       │   ├── Permission.java           #   值对象（属于 Role 的上下文）
│       │   ├── PermissionType.java       #   枚举
│       │   └── RoleRepository.java       #   仓储接口
│       ├── menu/                         # 菜单聚合（独立包——从 admin 拆出）
│       │   ├── Menu.java                 #   聚合根
│       │   ├── MenuType.java             #   枚举
│       │   └── MenuRepository.java       #   仓储接口
│
├── mall-application/                     # 应用层
│   └── application/
│       ├── command/                      # 写操作（按聚合扁平分包）
│       │   ├── product/
│       │   │   ├── ProductApplicationService.java   #   createProduct / publishProduct / changePrice
│       │   │   ├── CreateProductCommand.java        #   命令入参（不可变）
│       │   │   ├── ChangePriceCommand.java
│       │   │   └── PublishProductCommand.java
│       │   ├── order/
│       │   │   ├── OrderApplicationService.java     #   createOrder / payOrder / cancelOrder
│       │   │   ├── CreateOrderCommand.java          #   命令入参（不可变，含内嵌 OrderItemParam / ShippingAddressParam）
│       │   │   ├── PayOrderCommand.java
│       │   │   └── CancelOrderCommand.java
│       │   └── auth/
│       │       ├── AuthApplicationService.java      #   adminLogin / memberLogin
│       │       ├── AdminLoginCommand.java
│       │       ├── AdminLoginResult.java            #   命令出参（有返回值的用例就近放 *Result）
│       │       ├── MemberLoginCommand.java
│       │       └── MemberLoginResult.java
│       ├── query/                        # 读操作（CQRS）
│       │   ├── order/
│       │   │   ├── OrderQueryService.java           #   orderList / orderDetail
│       │   │   ├── OrderListQuery.java              #   查询入参（不可变筛选字段 + 继承 PageQuery 分页）
│       │   │   └── dto/
│       │   │       ├── OrderDetailDto.java          #   @Getter @Builder（面向展示的读模型）
│       │   │       ├── OrderListItemDto.java        #   金额字段统一 BigDecimal
│       │   ├── product/
│       │   │   ├── ProductQueryService.java         #   productList / productDetail / searchProducts / recommendProducts / hotProducts / categories
│       │   │   ├── ProductListQuery.java            #   分页 + categoryId + status
│       │   │   ├── ProductSearchQuery.java          #   分页 + keyword
│       │   │   └── dto/
│       │   │       ├── ProductDetailDto.java
│       │   │       ├── ProductListItemDto.java
│       │   │       ├── ProductSkuDto.java
│       │   │       ├── CategoryDto.java
│       │   ├── admin/
│       │   │   ├── AdminQueryService.java           #   adminList（3参数暂用裸参数）
│       │   │   ├── MenuQueryService.java            #   menuTree / menuTreeByPermissionCodes
│       │   │   ├── RoleQueryService.java            #   roleList
│       │   │   └── dto/
│       │   │       ├── AdminListItemDto.java        #   @Getter @Builder
│       │   │       ├── MenuTreeDto.java
│       │   │       ├── RoleListItemDto.java
│       │   ├── dashboard/
│       │   │   ├── DashboardQueryService.java       #   dashboardStats
│       │   │   └── dto/
│       │   │       └── DashboardStatsDto.java
│       │   └── support/
│       │       ├── PageQuery.java                   #   分页入参基类（@Getter @Setter 可变）
│       │       └── PageResult.java                  #   分页出参（@Getter @Builder，字段名 pageNum/pageSize/totalCount/data）
│       ├── eventhandler/                 # 领域事件处理器（保留 *EventHandler 命名）
│           └── order/
│               ├── OrderCreatedEventHandler.java
│               ├── OrderPaidEventHandler.java
│               └── OrderCancelledEventHandler.java
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
        │   ├── product/ProductController.java   #   仅注入 ProductApplicationService + ProductQueryService
        │   ├── product/StorefrontController.java #   仅注入 ProductQueryService
        │   ├── order/OrderController.java
        │   ├── admin/AdminController.java
        │   ├── admin/DashboardController.java    #   仅注入 DashboardQueryService
        │   └── auth/AuthController.java
        ├── request/                     # HTTP 请求体 DTO（含 @Valid）
        └── response/
            ├── ApiResponse.java          #   统一响应封装
            └── PageResponse.java         #   已废弃——Controller 直接返回 application 层 PageResult
```

### 4.4 分包原则

| 原则 | 说明 |
|------|------|
| **领域层按聚合分包** | `domain/order/`、`domain/product/`、`domain/role/`，而非 `domain/entity/`、`domain/vo/`；一个聚合根一个包，包内子包按需升级（event/、query/） |
| **领域层聚合根在包顶层** | 聚合根 + 内部实体/值对象平铺在包顶层；event/ 和 query/ 是子包；一眼看到统领者 |
| **仓储接口只保留命令侧操作** | `findById/findByXxx/save` → 命令侧；`findPage/countTotal` → 查询侧 QueryPort |
| **共享概念提取到 shared/** | `CommonStatus`（ENABLED/DISABLED）跨聚合共用，放在 shared/ |
| **值对象不继承基类** | 用 Lombok `@Getter @AllArgsConstructor @EqualsAndHashCode` + final 字段 + 构造函数校验，无需 `extends ValueObject` |
| **应用层按聚合扁平分包** | `command/order/`、`query/order/`、`eventhandler/order/`；同聚合的 `*ApplicationService` / `*Command` / `*Result` 平铺在一起，不再嵌套 `cmd/`、`handler/`、`result/` |
| **接口层按聚合分包** | `controller/order/`、`request/order/` |
| **基础设施层按技术关注点分包** | `dataobject/`、`converter/`、`impl/` |
| **所有 ApplicationService 方法必须接受 Command 对象** | 例如 `payOrder(PayOrderCommand command)` 而非 `payOrder(String orderNo)`，保持入参类型安全与可扩展性 |
| **所有 QueryService 分页方法使用 *Query 对象** | 例如 `orderList(OrderListQuery query)` 而非 `orderList(int, int, String, String)`，4+ 参数必须升级为 *Query |
| **金额字段统一使用 BigDecimal** | DTO 中金额字段一律用 `BigDecimal`，禁止 `Double`（精度丢失风险） |
| **DTO 使用 Builder 构建** | `@Getter @Builder @NoArgsConstructor @AllArgsConstructor`，不用 `@Setter` |

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
| Web Controller | `*Request`（HTTP 请求体 DTO） | `ApiResponse<T>` / 直接返回 `PageResult<T>` |
| 应用层命令侧 | `*Command` | `*Result` / 原始类型 / `void` |
| 应用层查询侧 | `*Query`（多字段时）/ 裸参数（0~3 个） | `*Dto`、`PageResult<*Dto>` |
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
│   ├── MenuQueryService.java
│   ├── RoleQueryService.java
│   └── dto/
│       ├── AdminListItemDto.java       ← 出参（读模型）
│       └── ...
├── order/
│   ├── OrderQueryService.java
│   ├── OrderListQuery.java             ← 入参（4 参数 → 升级为 *Query）
│   └── dto/
│       ├── OrderListItemDto.java
│       └── OrderDetailDto.java
├── product/
│   ├── ProductQueryService.java
│   ├── ProductListQuery.java
│   ├── ProductSearchQuery.java
│   └── dto/
│       ├── ProductDetailDto.java
│       ├── ProductListItemDto.java
│       └── ...
├── dashboard/
│   ├── DashboardQueryService.java
│   └── dto/
│       └── DashboardStatsDto.java
└── support/
    ├── PageQuery.java                  ← 分页入参基类
    └── PageResult.java                 ← 分页出参
```

**`*Query` 的不可变性设计**：筛选字段使用 `final`（与 `*Command` 对称），分页字段继承 `PageQuery`（可变，因为分页参数允许外部设置默认值）：

```java
// PageQuery —— 分页基类，分页字段允许设置默认值，因此用可变模式
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery {
    private int pageNum = 1;
    private int pageSize = 10;
}

// OrderListQuery —— 筛选字段不可变，分页字段继承可变基类
@Getter
@RequiredArgsConstructor
public class OrderListQuery extends PageQuery {
    private final String status;       // 不可变筛选条件
    private final String keyword;      // 不可变筛选条件
}
```

**何时升级到 `*Query`，何时保留原始参数**（与 4.4.3 决策表对称）：

| 入参情况 | 做法 | 本项目对应 |
|---|---|---|
| 0 ~ 1 个原始类型参数 | 直接传 | `MenuQueryService.menuTree()`、`OrderQueryService.orderDetail(String orderNo)` |
| 2 ~ 3 个原始类型参数 | 直接传 | `RoleQueryService.roleList(int page, int size)`、`AdminQueryService.adminList(int page, int size, String keyword)` |
| ≥ 4 个参数 / 内聚业务含义 / 需要 Bean Validation / 字段还会扩展 | 建 `*Query` 类 | `OrderQueryService.orderList(OrderListQuery)`——原 4 个裸参数已升级 |
| 分页 + 筛选条件组合 | 建 `*Query` 类 extends `PageQuery` | `ProductListQuery(categoryId, status)`、`ProductSearchQuery(keyword)` |
| 多组互斥搜索条件（搜索 / 报表场景） | 建 `*Query` 类，可配 `@AssertTrue` 等校验 | 如未来 `ProductSearchQuery` |

> 参考：[项目结构](https://docs.mryqr.com/ddd-project-structure/) | [CQRS](https://docs.mryqr.com/ddd-cqrs/)

---

### 4.5 为什么这样分包——深层理由

新人打开一个 DDD 项目，最困惑的往往是：**为什么文件夹是这样组织的？** 以下逐层解释每个关键分包决策的理由，帮助你理解这些结构不是"随意排列"，而是映射了真实的业务边界和职责分层。

#### 4.5.1 为什么 `command/` 和 `query/` 分开（CQRS 的物理体现）

命令改变状态，查询只读数据——这是两种**截然不同的职责**。分文件夹不是"多建一个包"的形式主义，而是让新人一眼看到"这个项目读写是分离的"。

**如果不分开**会发生什么？

```java
// ❌ 混在一起：查询方法里写业务逻辑、命令方法里返回 DTO
public class OrderService {
    public OrderDetailDto getOrder(Long id) {  // 读
        Order order = repo.findById(id);
        return toDto(order);  // "读"和"写"的逻辑混在同一个类
    }
    public void payOrder(PayCommand cmd) {     // 写
        Order order = repo.findByOrderNo(cmd.getOrderNo());
        order.pay();
        repo.save(order);
    }
}
```

问题：
- 查询方法不需要事务，命令方法需要——混在一起会让 `@Transactional` 的语义混乱
- 查询可以绕过聚合根（直接查库返回 DTO），命令必须经过聚合根——两者的技术路线根本不同
- 查询的入参是 `*Query`，命令的入参是 `*Command`——混在一起名字会撞车

**分开后**：

```
command/order/  ← 只看命令逻辑，事务注解一目了然
query/order/    ← 只看查询逻辑，readOnly=true 一目了然
```

> 参考 mryqr 原文："[CQRS 核心思想：命令不返回数据，查询不改变状态](https://docs.mryqr.com/ddd-cqrs/)"

#### 4.5.2 为什么按聚合扁平分包，不按技术角色分包

mryqr 的核心观点："**先业务，后技术**"。这意味着包路径的第一维度是**业务聚合**，而不是技术角色。

对比两种方案：

```
❌ 按技术角色分包：改一个功能要跨 3 个包
command/
├── handler/             ← 所有 Handler
├── cmd/                 ← 所有 Command
├── result/              ← 所有 Result

✅ 按聚合扁平分包 + 后缀命名：改订单功能只看一个包
command/
├── order/
│   ├── OrderApplicationService.java  ← 后缀识别角色
│   ├── CreateOrderCommand.java       ← 后缀识别角色
│   ├── PayOrderCommand.java
│   └── CancelOrderCommand.java
├── product/
│   ├── ProductApplicationService.java
│   ├── CreateProductCommand.java
│   └── ChangePriceCommand.java
│   └── PublishProductCommand.java
```

**扁平分包 + 后缀命名 = 既保持业务内聚，又用类名区分角色**。

- 改订单相关功能 → 只看 `command/order/` 这个包
- 看到后缀 `*Command` → 知道是入参；看到 `*ApplicationService` → 知道是服务
- 不需要嵌套 `cmd/`、`handler/`、`result/` 三个子包，因为**包路径维度**已经包含了 `command/`，再嵌套就是重复

> 详细论证见 4.4.1 "为什么 `*Command` 后缀不冗余"

#### 4.5.3 为什么 `dto/` 单独成子包，`*Query` 不放进 `dto/`

```
query/order/
├── OrderQueryService.java       ← 服务
├── OrderListQuery.java          ← 入参（同包平铺）
└── dto/
    ├── OrderDetailDto.java      ← 出参（集中管理）
    └── OrderListItemDto.java
```

**理由**：

- `dto/` 是"读模型出参"的集中营——看 `dto/` 就知道这个聚合对外暴露了什么数据形态。如果有人想了解"订单查询返回什么字段"，他只需要打开 `dto/` 包看一遍
- `*Query` 是"读模型入参"——和出参是**两个方向**（一进一出），混在一起会模糊目录语义
- 入参（`*Query`）与服务（`*QueryService`）同包平铺，因为它们是**一起使用的**——构造 Query 对象、调用 Service 方法，这一对操作在 Controller 中总是一起出现

#### 4.5.4 为什么 `eventhandler/` 独立于 `command/` 和 `query/`

mryqr 明确说："事件处理器的地位相当于应用服务"。但触发方式不同：

| 维度 | 命令 | 查询 | 事件处理器 |
|------|------|------|------------|
| **触发方式** | Controller 主动调用 | Controller 主动调用 | 领域对象被动触发 |
| **入参** | `*Command` | `*Query` / 裸参数 | `*Event` |
| **事务** | `@Transactional` | `@Transactional(readOnly=true)` | `@Transactional` |
| **是否改变状态** | 是 | 否 | 是 |

命令由外部世界主动发起（"我要支付订单"），事件由领域对象被动触发（"订单已创建"）。分文件夹体现**"入口不同"**这个本质差异——新人一看就知道：command 里的是 Controller 直接调的，eventhandler 里的是 Spring 事件监听器自动调的。

#### 4.5.5 为什么 `support/` 只放 `PageQuery` 和 `PageResult`

分页是**跨聚合的通用机制**，不属于任何特定业务。如果把分页类放在每个聚合包里，会造成大量重复。`PageQuery` / `PageResult` 是"应用层基础设施"，与 domain 层的 `shared/` 对称——都是跨业务聚合的通用支撑。

```
domain/shared/         ← 领域层通用基类（AggregateRoot, Entity, DomainEvent, Money, CommonStatus）
application/query/support/ ← 应用层通用分页（PageQuery, PageResult）
```

#### 4.5.6 为什么领域层"一个聚合根一个包"，而不是"按技术角色分包"

这是 mryqr 最核心的分包原则："**先业务，后技术**"。领域层的包第一维度必须是**业务聚合**，而不是技术角色。

**按聚合分包 vs 按技术角色分包**：

```
❌ 按技术角色分包：改一个聚合要看 N 个包
domain/
├── aggregate/          ← 所有聚合根
├── entity/             ← 所有实体
├── valueobject/        ← 所有值对象
├── repository/         ← 所有仓储接口
├── event/              ← 所有事件
├── enum/               ← 所有枚举

✅ 按聚合分包 + 子包：改一个聚合只看一个包
domain/
├── order/              ← 订单聚合的所有东西
│   ├── Order.java          ← 聚合根（顶层，一眼看到统领者）
│   ├── OrderItem.java      ← 内部实体
│   ├── ShippingAddress.java ← 内部值对象
│   ├── OrderRepository.java ← 仓储接口
│   ├── OrderStatus.java    ← 枚举
│   ├── query/              ← CQRS 查询端口
│   └── event/              ← 事件子包
├── product/
├── role/
├── menu/
```

**为什么聚合根在包顶层、内部对象也平铺在顶层**：

- 聚合根是包的"统领者"——包名即聚合名（`order/` = Order 聚合），聚合根类在顶层一眼可见
- 内部实体（OrderItem）和值对象（ShippingAddress）也平铺在顶层——因为它们是聚合根的**内部对象**，对外黑盒，但在包内是平级的
- mryqr: "外部对聚合根的调用只能通过根对象完成"——但包内部的组织不需要隐藏

**为什么 `event/` 和 `query/` 是子包而非平铺**：

- 事件数量可能增多（Order 有 3 个事件），独立子包更清晰
- QueryPort 是查询侧的独立职责（与仓储接口对称），独立子包一眼区分"写端口 vs 读端口"
- 如果只有 1~2 个事件或枚举，可以放顶层；多了再升级为子包

#### 4.5.7 为什么 Admin / Role / Menu 拆成三个独立包

**当前问题**：原来 `admin/` 包混合了 3 个聚合根（Admin、Role、Menu）+ 3 个仓储 + 2 个枚举 + 1 个值对象，全部平铺。这违反了"一个聚合根一个包"原则。

**拆分理由**：

1. **独立生命周期**：Admin（管理员）、Role（角色）、Menu（菜单）各有独立的创建、修改、删除生命周期。一个 Role 可以独立于 Admin 存在（被分配给不同 Admin），一个 Menu 可以独立于 Role 存在（只用于导航）
2. **独立仓储**：三个聚合各自有独立的 Repository——这是它们是独立聚合根的**最直接证据**
3. **独立统一语言**：Admin 的语言是"管理员/密码/角色分配"，Role 的语言是"角色/权限/权限码"，Menu 的语言是"菜单/权限码/树形结构"。混在一起会让统一语言模糊
4. **微服务迁移准备**：如果未来需要拆分微服务，Admin/Role/Menu 可能是不同的服务边界——独立包就是天然的拆分点

**`AdminStatus` 的处理**：

原来 `AdminStatus`（ENABLED / DISABLED）被 Admin 和 Role 共用。拆分后，这种通用状态概念提取到 `shared/CommonStatus`——因为"启用/停用"不是某个聚合独有的概念，而是跨聚合的通用语言。

```
domain/shared/CommonStatus.java    ← 通用状态（ENABLED, DISABLED）
domain/admin/Admin.java            ← 使用 CommonStatus
domain/role/Role.java               ← 使用 CommonStatus
```

#### 4.5.8 为什么仓储接口只保留命令侧操作，查询侧用 QueryPort

mryqr: "资源库以聚合根为单位完成对数据库的访问，参数和返回的数据都应该是聚合根对象"。

这意味着 `findPageForAdmin()`（返回分页列表）、`countTotal()`（返回 long）这些方法不属于仓储——它们的返回值不是聚合根。

**CQRS 的物理体现**：

```
命令侧（Repository）                    查询侧（QueryPort）
├── findById → Optional<Order>           ├── findPageForAdmin → OrderPageResult
├── findByOrderNo → Optional<Order>      ├── countTotal → long
├── save(Order) → void                   ├── sumTotalAmountSince → BigDecimal
```

QueryPort 与 Repository 同级——都在领域层定义接口，基础设施层实现。应用层 QueryService 注入两者：
```java
private final OrderRepository orderRepository;    // 命令侧：加载单个聚合根
private final OrderQueryPort orderQueryPort;      // 查询侧：分页/统计/搜索
```

---

### 4.6 入参出参不可变性规范

不同的入参出参类型有不同的不可变性要求，这是**有意为之的设计**，而非随意选择：

| 类型 | 不可变性 | Lombok 组合 | 原因 |
|------|----------|-------------|------|
| `*Command` | **不可变** | `@Getter @RequiredArgsConstructor`（final 字段） | 命令一旦创建不应修改——代表一个确定的业务意图 |
| `*Query` | **筛选字段不可变，分页字段可变** | `@Getter @RequiredArgsConstructor`(final) + `extends PageQuery` | 筛选条件不应中途变化；分页参数允许前端设置默认值 |
| `*Result` | **不可变** | `@Getter @RequiredArgsConstructor`（final 字段） | 结果不应被篡改 |
| `*Dto`（读模型） | **可变（Builder）** | `@Getter @Builder @NoArgsConstructor @AllArgsConstructor` | Builder 便于 QueryService 组装；NoArgsConstructor 便于 Jackson 序列化 |
| `PageQuery` | **可变** | `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` | 分页字段允许外部设置（Controller 中 `query.setPageNum(page)`） |
| `PageResult` | **可变（Builder）** | `@Getter @Builder @NoArgsConstructor @AllArgsConstructor` | Builder 便于 QueryService 组装 |

**为什么 `*Dto` 用 Builder 而不是 `@Setter`？**

旧做法用 `@Setter` 导致 DTO 可被任意修改（`dto.setStatus("PAID")`），语义脆弱。改用 Builder 后：
- 构建 DTO 只能通过 `OrderDetailDto.builder().id(x).orderNo(y)...build()`——构建路径明确、字段不会遗漏
- 构建完成后 DTO 不会再被修改（虽然技术上可以 setter，但团队约定 DTO 构建后不再修改）
- `@NoArgsConstructor + @AllArgsConstructor` 确保 Jackson 序列化/反序列化正常

---

### 4.7 层间隔离——常见违规与修复

DDD 项目最常见的架构腐化就是**层间穿透**——某个层直接访问了不该访问的层。以下展示本项目曾出现的典型违规及其修复。

#### ❌ 违规案例：ProductController 直接注入 Infrastructure 层 JPA Repository

```java
// ❌ Controller 直接访问 Infrastructure 层，跳过 Application 和 Domain
@RestController
public class ProductController {
    private final ProductJpaRepository productJpaRepository;  // ← Infrastructure 层类！

    @GetMapping
    public ApiResponse<PageResponse<ProductView>> listProducts(...) {
        List<ProductDO> filtered = productJpaRepository.findAll().stream()  // ← 业务逻辑在 Controller
                .filter(p -> isVisibleStatus(p, status))
                .filter(p -> matchesKeyword(p, keyword))
                .collect(Collectors.toList());
        return ApiResponse.ok(toPageResponse(filtered, page, size));  // ← 分页逻辑也在 Controller
    }

    private ProductView toProductView(ProductDO product, ...) {  // ← DTO 转换也在 Controller
        // ~30 行映射逻辑...
    }
}
```

**三条红线全部踩了**：
1. Web 层直接引用 Infrastructure 层（`ProductJpaRepository`）——依赖方向从 web → infrastructure，跳过了 application 和 domain
2. ~150 行业务逻辑（过滤、分页、DTO 转换）写在 Controller 中——Controller 应该极薄
3. `ProductView` 等 DTO 只在 Web 层定义——查询服务没有对应的读模型

#### ✅ 修复后：所有查询通过 Application 层 QueryService

```java
// ✅ Controller 极薄：构造 Query → 调用 QueryService → 返回结果
@RestController
public class ProductController {
    private final ProductQueryService productQueryService;  // ← 只依赖 Application 层

    @GetMapping
    public ApiResponse<PageResult<ProductListItemDto>> listProducts(...) {
        ProductListQuery query = new ProductListQuery(categoryId, status);
        query.setPageNum(page);
        query.setPageSize(size);
        return ApiResponse.ok(productQueryService.productList(query));  // ← 逻辑全在 QueryService
    }
}
```

**修复要点**：
- Controller 不再注入任何 Infrastructure 层类
- 所有查询逻辑（过滤、分页、DTO 转换）移到 `ProductQueryService`
- `ProductQueryService` 通过**领域 Repository 接口**（`ProductRepository`）访问数据，而非直接用 JPA
- `ProductListItemDto` 等读模型在 Application 层定义，Web 层直接复用

#### ❌ 违规案例：DashboardStatsService 在 Web 层直接访问 3 个 JPA Repository

```java
// ❌ Web 层 Service 直接访问 Infrastructure
@Service
public class DashboardStatsService {
    private final ProductJpaRepository productJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
}
```

#### ✅ 修复后：DashboardQueryService 在 Application 层通过领域 Repository 接口

```java
// ✅ Application 层通过领域接口访问数据
@Service
public class DashboardQueryService {
    private final ProductRepository productRepository;    // ← 领域接口
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
}
```

**原则总结**：Controller / Web 层 Service **不应注入任何 Infrastructure 层的类**。所有数据访问必须经过 Application 层，Application 层通过领域 Repository 接口访问数据。这样保证了依赖方向始终是 **web → application → domain ← infrastructure**。

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
Controller → 构造 *Query → QueryService → Repository → 返回 *Dto / PageResult<*Dto>
```

```java
// Controller：极薄，只做 Request → Query 转换 + 调用 QueryService
@GetMapping
public ApiResponse<PageResult<OrderListItemDto>> orderList(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword) {
    OrderListQuery query = new OrderListQuery(status, keyword);
    query.setPageNum(page);
    query.setPageSize(size);
    return ApiResponse.ok(orderQueryService.orderList(query));
}

// QueryService：查询 + 投影到 DTO（使用 Builder 构建）
@Transactional(readOnly = true)
public PageResult<OrderListItemDto> orderList(OrderListQuery query) {
    int safePage = Math.max(query.getPageNum(), 1);
    int safeSize = Math.max(query.getPageSize(), 10);
    OrderPageSlice slice = orderRepository.findPageForAdmin(safePage, safeSize, query.getStatus(), query.getKeyword());
    List<OrderListItemDto> content = slice.getContent().stream().map(this::toListItemDto).toList();
    return PageResult.<OrderListItemDto>builder()
            .data(content)
            .totalCount(slice.getTotalElements())
            .totalPages(...)
            .pageNum(safePage)
            .pageSize(safeSize)
            .build();
}

private OrderListItemDto toListItemDto(Order order) {
    return OrderListItemDto.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .status(toApiStatus(order.getStatus()))
            .totalAmount(order.getTotalAmount().getAmount())  // BigDecimal，不是 doubleValue()
            .items(...)
            .build();
}
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
| **属性必须有业务注释** | 每个字段用 `/** */` 注释说明业务含义 | `/** 订单号，系统生成的唯一标识 */` 而不是沉默的字段 |
| **禁止手写 protected 无参构造函数** | 用 `@ReconstructionOnly` + `@NoArgsConstructor(access = PROTECTED)` 替代 | 见 6.2.1 和 6.2.2 |

#### 6.2.1 聚合根属性必须有业务注释

聚合根的每个字段都应有 `/** */` 注释，说明其**业务含义**而非技术含义。这对教学项目和团队协作至关重要——注释应回答"这个字段在业务上是什么"，而不是重复字段名：

```java
/** 订单号，系统生成的唯一标识 */
private String orderNo;
/** 下单会员ID，跨聚合引用 */
private Long memberId;
/** 订单总金额，由订单项自动计算 */
private Money totalAmount;
/** 订单状态，驱动业务流转的状态机 */
private OrderStatus status;
```

**注释要点**：

- 说明业务角色，如"跨聚合引用"标明引用关系而非整体持有
- 说明值的来源，如"由订单项自动计算"标明非外部传入
- 说明值的含义，如"已被订单预占但未扣减"而非只写"锁定数量"

#### 6.2.2 用 `@ReconstructionOnly` + `@NoArgsConstructor` 替代手写 protected 构造函数

聚合根的 protected 无参构造函数仅供仓储重建（从数据库加载时 JPA/Converter 反射创建对象）。过去我们手写 `protected Order() {}`，现在用注解替代：

```java
/**
 * 订单聚合根
 * 状态机：PENDING_PAYMENT → PAID → SHIPPED → COMPLETED | CANCELLED
 */
@Getter
@ReconstructionOnly
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends AggregateRoot {
    // ... 字段和业务方法
}
```

**两个注解各自的作用**：

| 注解 | 作用 | 来源 |
|------|------|------|
| `@NoArgsConstructor(access = AccessLevel.PROTECTED)` | **生成** protected 无参构造函数（Lombok 机械行为） | Lombok |
| `@ReconstructionOnly` | **声明** 设计意图——"此构造函数仅供仓储重建，业务代码禁止使用" | 本项目自定义（`domain.shared`） |

**为什么需要 `@ReconstructionOnly`**？Lombok 的 `@NoArgsConstructor` 只管生成代码，无法表达意图。手写 `protected Order() {}` 时开发者至少能看到一行代码，而 Lombok 生成后这行代码消失了，新人无法知道"为什么会有一个 protected 无参构造函数"。`@ReconstructionOnly` 用 `@Documented` + `@Retention(SOURCE)` 在源码层面声明意图，编译后消失——它不是运行时约束，而是**给人的提醒**。

### 6.3 仓储模式——命令侧端口

**核心思想：领域层定义接口，基础设施层实现。仓储接口只保留命令侧操作。**

```java
// 领域层：只声明"我需要什么能力"——命令侧（加载/保存聚合根）
public interface OrderRepository {
    Optional<Order> findById(Long id);
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByMemberId(Long memberId);
    void save(Order order);
}

// 基础设施层：决定"怎么实现"
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void save(Order order) {
        OrderDO saved = jpaRepository.save(OrderConverter.toDO(order));
        order.setId(saved.getId());
        order.setVersion(saved.getVersion());
        order.getDomainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();
    }
}
```

**仓储接口瘦身原则**：mryqr观点——"资源库以聚合根为单位完成对数据库的访问"，**参数和返回值都应该是聚合根对象**。

| 方法类型 | 是否属于仓储 | 原因 |
|----------|-------------|------|
| `findById` → 聚合根 | ✅ 是 | 命令侧需要加载聚合根才能操作 |
| `findByOrderNo` → 聚合根 | ✅ 是 | 同上，只是按不同条件加载 |
| `save` → void | ✅ 是 | 命令侧需要持久化聚合根 |
| `findPageForAdmin` → 分页列表 | ❌ 不是 | 这是查询侧需求，返回的不是单个聚合根 |
| `countTotal` → long | ❌ 不是 | 统计查询，与聚合根无关 |
| `sumTotalAmountSince` → BigDecimal | ❌ 不是 | 统计聚合，与聚合根无关 |

### 6.4 CQRS 查询端口（QueryPort）

CQRS 的核心是**命令和查询走不同路径**。仓储接口（Repository）是命令侧的端口，查询侧需要独立的端口接口——**QueryPort**。

```java
// 领域层：查询端口接口（读侧）——同样是领域层定义接口，基础设施层实现
package com.ddd.mall.domain.order.query;

public interface OrderQueryPort {
    OrderPageResult findPageForAdmin(int page, int size, String statusApi, String keyword);
    long countTotal();
    long countByCreatedAtSince(LocalDateTime since);
    BigDecimal sumTotalAmountSince(LocalDateTime since);
}

// 基础设施层：实现查询端口（直接用 JPA Specification / JPQL）
@Repository
@RequiredArgsConstructor
public class OrderQueryPortImpl implements OrderQueryPort {
    private final OrderJpaRepository jpaRepository;

    @Override
    public OrderPageResult findPageForAdmin(...) {
        Page<OrderDO> result = jpaRepository.findAll(spec, pageable);
        return new OrderPageResult(...);
    }
}
```

**为什么 QueryPort 放在领域层而非应用层**：
- QueryPort 是领域概念的查询抽象（"我需要按状态查订单"），不是应用层的技术编排
- 与 Repository 同级——两者都是领域层定义的端口接口，由基础设施层实现
- 应用层的 QueryService 通过 QueryPort 接口访问数据，不直接依赖 JPA Repository
- 这保持了依赖方向：`application → domain ← infrastructure`

**Repository 与 QueryPort 的对比**：

| 维度 | Repository | QueryPort |
|------|-----------|-----------|
| **职责** | 命令侧：加载/保存聚合根 | 查询侧：搜索/统计/分页 |
| **返回值** | 聚合根对象 | PageResult / 统计值 / List |
| **调用者** | ApplicationService（写） | QueryService（读） |
| **包位置** | 聚合包顶层 | 聚合包内 `query/` 子包 |
| **实现者** | Infrastructure层 | Infrastructure层 |

**应用层 QueryService 同时注入两个端口**：

```java
@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;    // 命令侧：加载单个聚合根（如详情查询）
    private final OrderQueryPort orderQueryPort;      // 查询侧：分页/统计/搜索
}
```

### 6.5 DO 转换（领域层纯净方案）

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

**本项目不使用 `ValueObject` 抽象基类**。值对象的契约通过以下方式保证：

```java
// Money —— 所有字段 final，没有 setter，运算返回新实例，构造函数校验
@Getter
@EqualsAndHashCode
public class Money {
    private final BigDecimal amount;                 // ← final，不可变
    private Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException("金额不能为空或负数"); // ← 构造函数校验
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    public Money add(Money other) { return new Money(this.amount.add(other.amount)); } // ← 返回新实例
}
```

**值对象的 Lombok 规范**：

```java
// ShippingAddress —— 典型值对象模式
@Getter
@AllArgsConstructor               // ← 构造函数赋值（全 final 字段）
@EqualsAndHashCode                 // ← 属性判等（值对象核心契约）
public class ShippingAddress {
    private final String receiverName;
    private final String province;
    private final String city;
    // ...
}
```

| 规范项 | 规则 | 原因 |
|--------|------|------|
| **所有字段 `final`** | 不可变，无 setter | 值对象一旦创建不应修改 |
| **`@EqualsAndHashCode`** | 属性判等 | 值对象靠属性值区分，不是靠 ID |
| **`@Getter @AllArgsConstructor`** | 只读 + 全参构造 | 不暴露 setter，只通过构造函数赋值 |
| **构造函数校验** | 创建时就保证合法 | "无论何时拿到值对象，都可以相信它是合法的" |
| **修改返回新实例** | `add/subtract` 返回新对象 | 不修改现有对象，而是创建新实例 |
| **不继承基类** | 不需要 `extends ValueObject` | Lombok `@EqualsAndHashCode` 已实现值对象契约，强制继承空抽象类无实际收益 |

**为什么不继承 ValueObject 基类**：

本项目原本定义了 `ValueObject` 抽象基类（声明 `equals/hashCode` 为抽象方法），但实际所有值对象（`Money`, `ShippingAddress`, `Address`, `Permission`）都用 Lombok `@EqualsAndHashCode` 实现判等，**从未继承过基类**。删除基类的理由：
1. Lombok `@EqualsAndHashCode` 已完整实现值对象的 equals/hashCode 契约
2. 强制继承空抽象类增加约束但无收益——值对象的关键特征是**不可变 + 属性判等 + 自校验**，不是"继承了某个基类"
3. mryqr文章也未要求值对象必须继承基类——关注行为而非继承层次

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

### 10.3 本项目的 CQRS 实现

**命令侧**（写操作）：

```java
// 命令入参：不可变，final 字段
@Getter
@RequiredArgsConstructor
public class CreateOrderCommand {
    private final Long memberId;
    private final List<OrderItemParam> items;
    private final ShippingAddressParam shippingAddress;
}

// 应用服务：编排，不包含业务逻辑
@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    @Transactional
    public String createOrder(CreateOrderCommand command) {
        // 从 Command 提取数据，传给领域对象（Command 不进入领域模型内部）
        List<OrderItem> items = command.getItems().stream()
                .map(p -> Order.createItem(...))
                .collect(Collectors.toList());
        Order order = new Order(orderNo, command.getMemberId(), items, shippingAddress);
        orderRepository.save(order);
        return orderNo;  // 轻量返回（单个原始类型）
    }
}
```

**查询侧**（读操作）—— 通过 QueryService + 领域 Repository 接口：

```java
// 查询入参：不可变筛选字段 + 可变分页基类
@Getter
@RequiredArgsConstructor
public class OrderListQuery extends PageQuery {
    private final String status;
    private final String keyword;
}

// 查询服务：通过领域 Repository 接口访问数据，投影为 DTO
@Service
@RequiredArgsConstructor
public class ProductQueryService {
    private final ProductRepository productRepository;  // ← 领域接口，不是 JPA

    @Transactional(readOnly = true)
    public PageResult<ProductListItemDto> productList(ProductListQuery query) {
        ProductPageSlice slice = productRepository.findPage(...);
        List<ProductListItemDto> content = slice.getContent().stream()
                .map(this::toListItemDto).toList();
        return PageResult.<ProductListItemDto>builder()
                .data(content)
                .totalCount(slice.getTotalElements())
                .build();
    }
}

// Controller：极薄，只做 Request → Query 转换
@GetMapping
public ApiResponse<PageResult<ProductListItemDto>> listProducts(...) {
    ProductListQuery query = new ProductListQuery(categoryId, status);
    query.setPageNum(page);
    query.setPageSize(size);
    return ApiResponse.ok(productQueryService.productList(query));
}
```

**关键原则**：
- **Controller 不应注入 Infrastructure 层的任何类**——所有查询必须经过 Application 层 QueryService
- **QueryService 通过领域 Repository 接口访问数据**——不直接用 JPA Repository
- **DTO 使用 Builder 构建、金额用 BigDecimal**——不用 @Setter、不用 Double

对于更复杂的查询场景（列表、分页、跨表 JOIN），可以在 Repository 接口中定义专门的查询方法（如 `findPageForAdmin`），实现类中使用 JPA Specification 或 JPQL。

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
| `@Getter @RequiredArgsConstructor` | Command、Event、Query、Result | 不可变数据载体，final 字段 |
| `@Getter @Builder @NoArgsConstructor @AllArgsConstructor` | DTO（读模型出参）、PageResult | Builder 便于服务端组装；NoArgsConstructor 便于 Jackson 序列化 |
| `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` | PageQuery（分页入参基类） | 分页字段允许外部设置默认值（Controller 中 `query.setPageNum(page)`） |
| `@RequiredArgsConstructor` | ApplicationService、DomainService、EventHandler | 构造器注入 |
| `@ReconstructionOnly @NoArgsConstructor(access = PROTECTED)` | 聚合根、聚合内实体 | Lombok 生成 protected 无参构造函数供仓储重建；`@ReconstructionOnly` 声明设计意图——业务代码禁止使用 |
| `@Slf4j` | 事件处理器 | 日志 |

### 12.2 禁止用法

| 注解 | 禁止用在哪里 | 原因 |
|------|-------------|------|
| `@Setter` | 聚合根 | `order.setStatus(PAID)` 绕过了业务规则，应该用 `order.pay()` |
| `@Data` | 聚合根、实体 | 等于 @Getter + @Setter + @ToString + @EqualsAndHashCode，setter 会破坏封装 |
| `@Builder` | 聚合根 | 构造应通过有业务含义的构造函数或工厂方法 |
| `@NoArgsConstructor(PUBLIC)` | 聚合根 | 允许创建无效状态的对象 |
| `protected Xxx() {}`（手写） | 聚合根、实体 | 用 `@ReconstructionOnly @NoArgsConstructor(access = PROTECTED)` 替代，避免手写与意图脱节 |

### 12.3 聚合根的 Setter 策略

聚合根的 setter 仅用于**仓储重建**（从数据库加载时设置字段）：

```java
@Getter                           // ✅ 所有字段可读
@ReconstructionOnly               // ✅ 声明 protected 构造函数仅供仓储重建
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // ✅ Lombok 自动生成 protected 无参构造
public class Order extends AggregateRoot {
    /** 订单号，系统生成的唯一标识 */
    @Setter private String orderNo;       // 仅供 Converter 重建用
    /** 下单会员ID，跨聚合引用 */
    @Setter private Long memberId;        // 仅供 Converter 重建用
    /** 订单状态，驱动业务流转的状态机 */
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

**团队约定**：
- 业务代码中禁止调用聚合根的 setter，setter 仅供 infrastructure 层 Converter 使用
- 禁止手写 `protected Xxx() {}`，用 `@ReconstructionOnly` + `@NoArgsConstructor(access = PROTECTED)` 替代
- 聚合根每个字段必须有 `/** */` 注释说明业务含义

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

**只定义命令侧业务需要的方法**，查询侧走 QueryService + QueryPort，不放在仓储接口里：

```java
// ✅ 好的仓储接口——只有命令侧操作：加载 + 保存
public interface ProductRepository {
    Optional<Product> findById(Long id);
    void save(Product product);
}

// ❌ 不好的仓储接口——混入查询方法 + 暴露技术细节
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findOnSaleProducts();    // 查询应在 QueryService 中
    List<Product> findByCategory(String category);  // 查询应在 QueryService 中
}
```

查询需求由 `ProductQueryService` + `ProductQueryPort`（基础设施层的查询接口）承载，仓储接口只保留"取出聚合根"和"保存聚合根"两类方法。

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
