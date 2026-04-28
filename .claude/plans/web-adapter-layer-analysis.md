# DDD-Mall-Server Web/Adapter Layer — Full Analysis & Refactoring Plan

## 1. Project Module Structure

4-module standard DDD architecture:

```
ddd-mall-server/
├── mall-domain          (Domain layer: entities, VOs, domain services, repo interfaces)
├── mall-application     (Application layer: command handlers, query services, event handlers, DTOs)
├── mall-infrastructure  (Infrastructure: JPA repos, auth, persistence DOs/converters)
├── mall-web             (Web/Adapter: controllers, requests, responses, service leak)
└── doc/                 (DDD guide & init SQL)
```

Package root: `com.ddd.mall`

---

## 2. All Controller Classes — Full Analysis

### 2.1 AdminController `/api/admin/admins`
- **Dependencies**: `AdminApplicationService`, `AdminQueryService`
- **Endpoints**:
  - `GET /` → `listAdmins(page, size, keyword)` — delegates to `adminQueryService.adminList()`, wraps `PageResult` → `PageResponse`
  - `POST /` → `createAdmin(CreateAdminRequest)` → constructs `CreateAdminCommand(username, password, realName, phone, email)`
  - `PUT /{id}/roles` → `assignRoles(id, AssignRolesRequest)` → constructs `AssignRolesCommand(id, roleIds)`
- **Auth**: `@RequireLogin(UserType.ADMIN)` class-level, `@RequirePermission("admin:create")`, `@RequirePermission("admin:assign-role")`
- **Status**: ✓ Clean — proper command construction and service delegation

### 2.2 AdminOrderController `/api/admin/orders`
- **Dependencies**: `OrderQueryService` only
- **Endpoints**: `GET /` → `listOrders(page, size, status, keyword)` — delegates to `orderQueryService.orderList()`
- **Auth**: `@RequireLogin(UserType.ADMIN)`, `@RequirePermission("order:view")`
- **Status**: ✓ Clean — read-only query delegation

### 2.3 DashboardController `/api/admin/dashboard` ⚠️ CRITICAL
- **Dependencies**: `DashboardStatsService` — lives in `com.ddd.mall.web.service` (⚠️ web layer, NOT application)
- **Endpoints**: `GET /stats` → calls `dashboardStatsService.load()` directly
- **Auth**: `@RequireLogin(UserType.ADMIN)`
- **⚠️ ISSUES**:
  1. Bypasses application layer entirely
  2. `DashboardStatsService` directly injects 3 infrastructure JPA repositories (`ProductJpaRepository`, `OrderJpaRepository`, `MemberJpaRepository`)
  3. Creates `DashboardStatsDto` in `web.response.admin` — response DTO in wrong package
  4. Should follow the same pattern as other query services (application layer)

### 2.4 MenuController `/api/admin/menus`
- **Dependencies**: `MenuApplicationService`, `MenuQueryService`
- **Endpoints**:
  - `POST /` → `createMenu(CreateMenuRequest)` → constructs `CreateMenuCommand(name, parentId, path, component, icon, permissionCode, type, sort)`
  - `GET /tree` → `getMenuTree()` — delegates to `menuQueryService.menuTree()`
- **Auth**: `@RequireLogin(UserType.ADMIN)`, `@RequirePermission("menu:create")` on POST
- **Status**: ✓ Clean

### 2.5 RoleController `/api/admin/roles`
- **Dependencies**: `RoleApplicationService`, `RoleQueryService`
- **Endpoints**:
  - `GET /` → `listRoles(page, size)` — delegates to `roleQueryService.roleList()`
  - `POST /` → `createRole(CreateRoleRequest)` → constructs `CreateRoleCommand(name, code, description)`
  - `PUT /{id}/permissions` → `assignPermissions(id, AssignPermissionsRequest)` → constructs `AssignPermissionsCommand(id, permissionCodes)`
- **Auth**: `@RequireLogin(UserType.ADMIN)`, `@RequirePermission` on writes
- **Status**: ✓ Clean

### 2.6 AuthController `/api/auth`
- **Dependencies**: `AuthApplicationService` only
- **Endpoints**:
  - `POST /admin/login` → `adminLogin(LoginRequest)` → constructs `AdminLoginCommand(username, password)`, returns `AdminLoginResult` directly
  - `POST /member/login` → `memberLogin(LoginRequest)` → constructs `MemberLoginCommand(username, password)`, returns `MemberLoginResult` directly
- **Auth**: No `@RequireLogin` (these ARE the login endpoints)
- **⚠️ Minor Issue**: `AdminLoginResult` / `MemberLoginResult` are application-layer types returned directly as HTTP response — layer boundary concern but pragmatic

### 2.7 OrderController `/api/orders`
- **Dependencies**: `OrderApplicationService`, `OrderQueryService`
- **Endpoints**:
  - `POST /` → `createOrder(CreateOrderRequest)` — **Complex nested mapping**:
    - `request.getItems().stream().map(i → new CreateOrderCommand.OrderItemParam(...))`
    - `request.getShippingAddress()` → `CreateOrderCommand.ShippingAddressParam(...)`
    - Returns `orderApplicationService.createOrder(command)` result
  - `POST /{orderNo}/pay` → constructs `PayOrderCommand(orderNo)` from path variable
  - `POST /{orderNo}/cancel` → constructs `CancelOrderCommand(orderNo)` from path variable
  - `GET /{orderNo}` → delegates to `orderQueryService.orderDetail(orderNo)`
- **Status**: ✓ Mostly clean — complex but correct command construction

### 2.8 ProductController `/api/products` ⚠️ MASSIVE VIOLATION
- **Dependencies**: `ProductApplicationService` (writes ✓), ⚠️ `ProductJpaRepository` (infrastructure — DIRECT injection)
- **Endpoints**:
  - `GET /` → `listProducts(page, size, categoryId, status)` ⚠️ calls `productJpaRepository.findAll()` then manually filters, sorts, paginates, converts `ProductDO` → `ProductView`
  - `GET /{id}` → `getProductDetail(id)` ⚠️ calls `productJpaRepository.findById()` directly
  - `GET /search` → `searchProducts(page, size, keyword)` ⚠️ calls `productJpaRepository.findAll()` + in-memory keyword filter
  - `POST /` → `createProduct(CreateProductRequest)` ✓ delegates to `productApplicationService.createProduct(command)`
  - `POST /{id}/publish` → ✓ delegates to `productApplicationService.publishProduct(command)`
  - `PUT /{id}/price` → ✓ delegates to `productApplicationService.changePrice(command)`
- **⚠️ ISSUES** (ALL critical):
  1. **Layer violation**: Directly injects `ProductJpaRepository` (infrastructure) — web layer should not know about infrastructure
  2. **Business logic in controller**: ~100 lines of filtering, sorting, pagination, category mapping, status mapping, attribute parsing, DTO conversion
  3. **Performance**: `findAll()` loads ALL products into memory for every request — no DB-level filtering/pagination
  4. **N+1 problem**: `buildCategoryIdMap()` does separate `findAll()` just to extract categories
  5. **Imports `ProductDO`/`ProductSkuDO`**: Infrastructure data objects used in web layer
  6. **Imports `ProductStatus`: Domain enum imported in controller for status mapping logic**

### 2.9 StorefrontController `/api` ⚠️ DUPLICATE VIOLATION
- **Dependencies**: ⚠️ `ProductJpaRepository` only — no application layer at all
- **Endpoints**:
  - `GET /categories` → builds category map from ALL products
  - `GET /home/recommend` → loads on-sale products, takes first 6
  - `GET /home/hot` → loads on-sale products, sorts by price+date, takes first 6
- **⚠️ ISSUES**:
  1. Only injects `ProductJpaRepository` — completely bypasses application/domain
  2. **5 methods 100% duplicated** from `ProductController`:
     - `buildCategoryIdMap()`
     - `toProductView()`
     - `toProductSkuView()`
     - `parseAttributes()`
     - `mapStatus()`
  3. Same performance issues (findAll() per request)

### 2.10 GlobalExceptionHandler
- Handles: `DomainException` → 400, `MethodArgumentNotValidException` → 400, `HttpRequestMethodNotSupportedException` → 405, generic `Exception` → 500
- Returns `ApiResponse<Void>` with `fail()` for all
- **Status**: ✓ Clean — appropriate for web layer

---

## 3. All Request Classes — Complete Inventory

| Class | Package | Fields | Validation |
|---|---|---|---|
| `CreateAdminRequest` | `web.request.admin` | username, password, realName, phone, email | @NotBlank(username, password, realName) |
| `AssignRolesRequest` | `web.request.admin` | roleIds: List&lt;Long&gt; | @NotNull |
| `AssignPermissionsRequest` | `web.request.admin` | permissionCodes: List&lt;String&gt; | @NotNull |
| `CreateMenuRequest` | `web.request.admin` | name, parentId, path, component, icon, permissionCode, type, sort | @NotBlank(name), @NotNull(type) |
| `CreateRoleRequest` | `web.request.admin` | name, code, description | @NotBlank(name, code) |
| `LoginRequest` | `web.request.auth` | username, password | @NotBlank on both |
| `CreateOrderRequest` | `web.request.order` | memberId, items, shippingAddress | @NotNull+@NotEmpty+@Valid |
| `CreateOrderRequest.OrderItemRequest` | (inner) | productId, skuId, productName, unitPrice, quantity | @NotNull+@Positive |
| `CreateOrderRequest.ShippingAddressRequest` | (inner) | receiverName, receiverPhone, province, city, district, detail | @NotNull on required |
| `CreateProductRequest` | `web.request.product` | name, description, price, category | @NotBlank+@NotNull+@Positive |
| `ChangePriceRequest` | `web.request.product` | newPrice | @NotNull+@Positive |

**Pattern**: All `@Getter @Setter`, Jakarta Validation, simple mutable POJOs. No record types used.

---

## 4. All Response/View Classes — Complete Inventory

| Class | Package | Fields | Style |
|---|---|---|---|
| `ApiResponse&lt;T&gt;` | `web.response` | success, message, data | Immutable (final), static factories `ok()/fail()` |
| `PageResponse&lt;T&gt;` | `web.response` | content, totalElements, totalPages, page, size | Mutable (@Getter @Setter @AllArgsConstructor) |
| `DashboardStatsDto` | `web.response.admin` | totalProducts, totalOrders, totalMembers, todayOrders, todayRevenue | Mutable (@AllArgsConstructor) |
| `ProductView` | `web.response.product` | id, name, description, mainImage, images, status, skus, categoryId, createdAt | Mutable (@AllArgsConstructor) |
| `ProductSkuView` | `web.response.product` | id, skuCode, attributes:Map, price, originalPrice, stock | Mutable (@AllArgsConstructor) |
| `CategoryView` | `web.response.product` | id, name, parentId, icon, children:List&lt;CategoryView&gt; | Mutable (@AllArgsConstructor) |

**Layer Boundary Issue**: Application-layer DTOs (`AdminLoginResult`, `MemberLoginResult`, `OrderDetailDto`, `OrderListItemDto`, `AdminListItemDto`, `MenuTreeDto`, `RoleListItemDto`) are returned directly as HTTP responses without web-layer adaptation. This makes application DTO structure = API contract.

---

## 5. Command Construction Patterns

### Pattern A — Simple Command (most common):
```java
CreateAdminCommand command = new CreateAdminCommand(
    request.getUsername(), request.getPassword(), request.getRealName(),
    request.getPhone(), request.getEmail());
return ApiResponse.ok(adminApplicationService.createAdmin(command));
```
Used in: AdminController, MenuController, RoleController, AuthController, ProductController (writes), OrderController (pay/cancel)

### Pattern B — Nested Command (OrderController):
```java
List<CreateOrderCommand.OrderItemParam> items = request.getItems().stream()
    .map(i -> new CreateOrderCommand.OrderItemParam(
        i.getProductId(), i.getSkuId(), i.getProductName(),
        i.getUnitPrice(), i.getQuantity()))
    .collect(Collectors.toList());

CreateOrderCommand.ShippingAddressParam shippingAddress = new CreateOrderCommand.ShippingAddressParam(
    addr.getReceiverName(), addr.getReceiverPhone(),
    addr.getProvince(), addr.getCity(), addr.getDistrict(), addr.getDetail());

return ApiResponse.ok(orderApplicationService.createOrder(
    new CreateOrderCommand(request.getMemberId(), items, shippingAddress)));
```

### Pattern C — Path Variable Command:
```java
orderApplicationService.payOrder(new PayOrderCommand(orderNo));
```
Used for pay, cancel, publish — only @PathVariable needed

### Pattern D — Query Delegation (clean):
```java
PageResult<AdminListItemDto> r = adminQueryService.adminList(page, size, keyword);
return ApiResponse.ok(new PageResponse<>(
    r.getContent(), r.getTotalElements(), r.getTotalPages(), r.getPage(), r.getSize()));
```
Converts `PageResult` (application) → `PageResponse` (web)

### Pattern E — Direct JPA Access (VIOLATED):
```java
List<ProductDO> filteredProducts = filterProducts(categoryId, status, null);
return ApiResponse.ok(toPageResponse(filteredProducts, page, size));
```
Controller directly queries infrastructure, manually builds response

---

## 6. DashboardStatsService — Complete Analysis

**Location**: `com.ddd.mall.web.service.DashboardStatsService`
**Source** (already read in full):

```java
@Service @RequiredArgsConstructor
public class DashboardStatsService {
    private final ProductJpaRepository productJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    public DashboardStatsDto load() {
        long totalProducts = productJpaRepository.count();
        long totalOrders = orderJpaRepository.count();
        long totalMembers = memberJpaRepository.count();
        var startOfDay = LocalDate.now().atStartOfDay();
        long todayOrders = orderJpaRepository.countByCreatedAtGreaterThanEqual(startOfDay);
        BigDecimal revenue = orderJpaRepository.sumTotalAmountSince(startOfDay);
        double todayRevenue = revenue == null ? 0d : revenue.doubleValue();
        return new DashboardStatsDto(totalProducts, totalOrders, totalMembers, todayOrders, todayRevenue);
    }
}
```

**5 Problems**:
1. **Wrong layer**: Lives in `web.service` — should be in `application.query.admin`
2. **Direct infrastructure access**: Injects 3 JPA repos — should use domain repository interfaces
3. **Wrong DTO location**: Creates `DashboardStatsDto` in `web.response.admin` — should be in `application.query.admin.dto`
4. **No application service**: Controller calls this directly — bypasses application layer
5. **Inconsistent pattern**: All other reads use `*QueryService` in application layer; this one doesn't

---

## 7. Complete Issue Inventory

### Critical Layer Violations

| Issue | Location | Impact |
|---|---|---|
| Web injects ProductJpaRepository (infrastructure) | ProductController, StorefrontController | Breaks DDD dependency rule |
| Web service injects 3 JPA repos (infrastructure) | DashboardStatsService | Breaks DDD dependency rule |
| ProductDO/ProductSkuDO used in web layer | ProductController, StorefrontController | Infrastructure data objects leak into adapter |
| ProductStatus (domain enum) used for mapping | ProductController, StorefrontController | Domain knowledge in adapter |

### Critical Business Logic in Controllers

| Logic | Lines | Duplication |
|---|---|---|
| Product filtering/sorting/pagination | ~30 lines | ×2 |
| Category ID map generation | ~15 lines | ×2 |
| Status mapping (ON_SALE→PUBLISHED etc.) | ~10 lines | ×2 |
| Attribute parsing (parseAttributes) | ~15 lines | ×2 |
| ProductDO→ProductView conversion | ~20 lines | ×2 |
| ProductSkuDO→ProductSkuView conversion | ~10 lines | ×2 |

### Severe Performance Issues

| Issue | Impact |
|---|---|
| `findAll()` per request — no DB-level filtering | O(N) memory, no LIMIT/OFFSET |
| `buildCategoryIdMap()` does separate `findAll()` for categories | Full table scan just for category names |
| In-memory pagination after loading all records | No DB pagination |
| `filterProducts()` loads ALL, then filters in Java | Should use JPA Criteria or JPQL WHERE |

### Architectural Inconsistency

| Path | Pattern | Status |
|---|---|---|
| Write operations | Request → Command → ApplicationService | ✓ Clean |
| Admin reads | Controller → QueryService → PageResult → PageResponse | ✓ Clean |
| Product reads | Controller → ProductJpaRepository → manual filter/convert | ✗ Broken |
| Dashboard reads | Controller → DashboardStatsService(web) → JPA repos | ✗ Broken |

### DTO Boundary Concerns

- Application-layer types (`AdminLoginResult`, `MemberLoginResult`, `OrderDetailDto`, `OrderListItemDto`, `AdminListItemDto`, `MenuTreeDto`, `RoleListItemDto`) returned directly as HTTP responses
- Makes application DTO structure part of the HTTP API contract
- Any DTO field change = API breaking change

---

## 8. Refactoring Plan

### Phase 1: Extract Product Query Logic → Application Layer

**Create `ProductQueryService`** in `mall-application/com.ddd.mall.application.query.product`:
- `productList(page, size, categoryId, status)` → `PageResult<ProductListItemDto>`
- `productDetail(id)` → `ProductDetailDto`
- `searchProducts(page, size, keyword)` → `PageResult<ProductListItemDto>`
- `categories()` → `List<CategoryDto>`
- `recommendProducts()` → `List<ProductListItemDto>`
- `hotProducts()` → `List<ProductListItemDto>`

**Create DTOs** in `application.query.product.dto`:
- `ProductListItemDto` (replaces `ProductView` for list queries)
- `ProductSkuListItemDto` (replaces `ProductSkuView`)
- `CategoryDto` (replaces `CategoryView`)

**Add domain repository methods**:
- `ProductRepository.findPage(page, size, categoryId, status)` or equivalent
- `ProductRepository.findById()` already exists
- `ProductRepository.findByKeyword(keyword, page, size)`
- `ProductRepository.findByStatus(status)` already exists in domain interface?
- Need category query support — either `ProductRepository.findDistinctCategories()` or separate `CategoryRepository`

**Refactor controllers**:
- `ProductController`: Remove `ProductJpaRepository`, remove all private helper methods, delegate reads to `ProductQueryService`
- `StorefrontController`: Same — remove all duplicated code, delegate to `ProductQueryService`

### Phase 2: Move DashboardStats → Application Layer

**Create `DashboardQueryService`** in `mall-application/com.ddd.mall.application.query.admin`:
- `stats()` → `DashboardStatsDto`

**Move DTO**: `DashboardStatsDto` from `web.response.admin` → `application.query.admin.dto`

**Add domain repository methods**:
- `ProductRepository.count()`
- `OrderRepository.count()` + `OrderRepository.countByCreatedAtSince(start)` + `OrderRepository.sumTotalAmountSince(start)`
- `MemberRepository.count()`

**Delete** `web.service.DashboardStatsService`

**Refactor `DashboardController`**: inject `DashboardQueryService` instead

### Phase 3: Clean Response Boundary (Optional)

**Option A (Strict layering)** — create web-layer response wrappers:
- `AdminLoginResponse` wrapping `AdminLoginResult`
- `MemberLoginResponse` wrapping `MemberLoginResult`
- etc.

**Option B (Pragmatic)** — accept application DTOs as API contract:
- Document this decision explicitly
- Ensure application DTOs are stable and designed for API use
- Keep current approach — minimal refactoring effort

**Recommendation**: Option B for this project scale, but add documentation.

### Phase 4: Fix Performance Issues

**In `ProductRepositoryImpl`** (infrastructure), add proper JPA queries:
- Use Spring Data `Pageable` for DB-level pagination
- Use `JpaSpecificationExecutor` or custom JPQL for filtering by category, status, keyword
- Add `findDistinctCategories()` query method
- Replace `findAll()` + in-memory filter with DB-level WHERE + LIMIT + OFFSET

**In `OrderRepositoryImpl`** (infrastructure):
- Already has `JpaSpecificationExecutor` — ensure dashboard methods use DB-level aggregation

### Phase 5: Duplication Eliminated

After Phase 1, ALL duplicated methods (5 methods × 2 controllers = 10 copies) move to `ProductQueryService`. Zero duplication remains.

---

## 9. Dependency Graph — Current vs Target

### Current (Violated):
```
ProductController → ProductJpaRepository (INFRASTRUCTURE) ⚡ VIOLATION
StorefrontController → ProductJpaRepository (INFRASTRUCTURE) ⚡ VIOLATION
DashboardController → DashboardStatsService (WEB) → 3× JPA Repos (INFRA) ⚡ VIOLATION
ProductController → ProductApplicationService (APPLICATION) ✓ writes only
```

### Target (Clean DDD):
```
ProductController → ProductQueryService (APPLICATION) → ProductRepository (DOMAIN INTERFACE)
ProductController → ProductApplicationService (APPLICATION) → ProductRepository (DOMAIN INTERFACE)
StorefrontController → ProductQueryService (APPLICATION) → ProductRepository (DOMAIN INTERFACE)
DashboardController → DashboardQueryService (APPLICATION) → Domain Repos (DOMAIN INTERFACE)
```

All infrastructure implementations injected by Spring at runtime — web layer never references them directly.