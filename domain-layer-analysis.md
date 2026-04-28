# Domain Layer Package Structure Analysis

## Complete File Inventory (42 Java files)

---

## 1. `admin/` Package — 10 files, FLAT (no sub-packages)

### Classes:
| File | Type | DDD Role | Notes |
|------|------|----------|-------|
| `Admin.java` | Class | **Aggregate Root** | `extends AggregateRoot`. Has `roleIds: List<Long>` (references Role by ID, not composition). |
| `Role.java` | Class | **Aggregate Root** | `extends AggregateRoot`. Has `permissionCodes: List<String>` (references Permission by code, not composition). |
| `Menu.java` | Class | **Aggregate Root** | `extends AggregateRoot`. Tree structure via `parentId` self-reference. |
| `Permission.java` | Class | **Value Object** | Immutable (`final` fields), `@EqualsAndHashCode`. But does NOT extend `ValueObject` base class. |
| `PermissionType.java` | Enum | **Enum (value object)** | MENU, BUTTON, API |
| `AdminStatus.java` | Enum | **Enum (value object)** | ENABLED, DISABLED — shared between Admin AND Role |
| `MenuType.java` | Enum | **Enum (value object)** | DIRECTORY, MENU, BUTTON |
| `AdminRepository.java` | Interface | **Repository** | Works over `Admin` aggregate root |
| `RoleRepository.java` | Interface | **Repository** | Works over `Role` aggregate root |
| `MenuRepository.java` | Interface | **Repository** | Works over `Menu` aggregate root |

### Organization: FLAT
- Everything is in `com.ddd.mall.domain.admin` with no sub-packages.
- **3 separate aggregate roots** (Admin, Role, Menu) are all dumped in the same flat package.
- **3 repository interfaces** (AdminRepository, RoleRepository, MenuRepository) all in the same package as their respective aggregates, but mixed together.
- **1 value object** (Permission) and **3 enums** (PermissionType, AdminStatus, MenuType) are mixed in with the aggregate roots and repositories.

### Critical Problem: Multi-Aggregate Package
This package contains **3 independent aggregates** (Admin, Role, Menu) plus a value object (Permission) that belongs to Role's aggregate conceptually. Per DDD, each aggregate should have its own package boundary. The current structure violates the aggregate-as-package-boundary principle.

---

## 2. `product/` Package — 6 files + `event/` sub-package (7 files total)

### Classes:
| File | Type | DDD Role | Notes |
|------|------|----------|-------|
| `Product.java` | Class | **Aggregate Root** | `extends AggregateRoot`. Composes `List<ProductSku>` (entities). Registers `ProductCreatedEvent`, `ProductPriceChangedEvent`. |
| `ProductSku.java` | Class | **Entity** | `extends Entity`. Belongs to Product aggregate. |
| `ProductStatus.java` | Enum | **Enum** | DRAFT, ON_SALE, OFF_SALE |
| `ProductPageSlice.java` | Class | **Read Model / DTO** | NOT a domain concept — pagination result container. Immutable (`final` fields, `@RequiredArgsConstructor`). |
| `ProductRepository.java` | Interface | **Repository** | Includes pagination method returning `ProductPageSlice`. |
| `event/ProductCreatedEvent.java` | Class | **Domain Event** | `implements DomainEvent`. References Product aggregate. |
| `event/ProductPriceChangedEvent.java` | Class | **Domain Event** | `implements DomainEvent`. References Product aggregate. |

### Organization: Events in sub-package, everything else flat
- Root: `com.ddd.mall.domain.product` — aggregate root, entity, enum, repository, page slice
- Sub-package: `com.ddd.mall.domain.product.event` — domain events
- **No `entity/`, `valueobject/`, `repository/` sub-packages** — entities and value objects mixed flat with the aggregate root.

### Anomaly: `ProductPageSlice`
This is a **read model / query DTO**, not a domain concept. It should NOT be in the domain layer — it belongs to the application layer or a dedicated query/read model package.

---

## 3. `order/` Package — 7 files + `event/` sub-package (9 files total)

### Classes:
| File | Type | DDD Role | Notes |
|------|------|----------|-------|
| `Order.java` | Class | **Aggregate Root** | `extends AggregateRoot`. Composes `List<OrderItem>` (entities) + `ShippingAddress` (value object). Registers 3 events. |
| `OrderItem.java` | Class | **Entity** | `extends Entity`. Belongs to Order aggregate. Has `subtotal()` method. |
| `OrderStatus.java` | Enum | **Enum** | State machine: PENDING_PAYMENT → PAID → SHIPPED → COMPLETED | CANCELLED |
| `ShippingAddress.java` | Class | **Value Object** | Immutable (`final` fields, `@AllArgsConstructor`, `@EqualsAndHashCode`). But does NOT extend `ValueObject` base class. |
| `OrderPageSlice.java` | Class | **Read Model / DTO** | Pagination result. NOT a domain concept. |
| `OrderRepository.java` | Interface | **Repository** | Includes pagination + admin stats queries. Uses `java.math.BigDecimal` and `LocalDateTime` directly in method signatures. |
| `event/OrderCreatedEvent.java` | Class | **Domain Event** | Contains inner static class `OrderItemInfo` (a mini-DTO within the event). |
| `event/OrderPaidEvent.java` | Class | **Domain Event** | Reuses `OrderCreatedEvent.OrderItemInfo` inner class (cross-event coupling). |
| `event/OrderCancelledEvent.java` | Class | **Domain Event** | Also reuses `OrderCreatedEvent.OrderItemInfo`. |

### Organization: Events in sub-package, everything else flat
- Same pattern as product: events get a sub-package, everything else is flat.
- `OrderItemInfo` inner class in `OrderCreatedEvent` is shared by `OrderPaidEvent` and `OrderCancelledEvent` — creates cross-event dependency.

### Anomalies:
- `OrderPageSlice` — same read-model-in-domain problem as `ProductPageSlice`.
- `OrderRepository` has admin-specific query methods (`findPageForAdmin`, `countByCreatedAtSince`, `sumTotalAmountSince`) — these are **application/use-case concerns**, not domain repository concerns. The repository interface is polluted with CQRS read-side queries.
- `ShippingAddress` is a value object but does NOT extend the `ValueObject` base class from `shared/`.

---

## 4. `inventory/` Package — 2 files + `event/` sub-package (4 files total)

### Classes:
| File | Type | DDD Role | Notes |
|------|------|----------|-------|
| `Inventory.java` | Class | **Aggregate Root** | `extends AggregateRoot`. Manages `totalStock`, `availableStock`, `lockedStock`. Registers `InventoryDeductedEvent`, `InventoryRestoredEvent`. |
| `InventoryRepository.java` | Interface | **Repository** | Clean: only `findByProductId` and `save`. |
| `event/InventoryDeductedEvent.java` | Class | **Domain Event** | `implements DomainEvent`. |
| `event/InventoryRestoredEvent.java` | Class | **Domain Event** | `implements DomainEvent`. |

### Organization: Events in sub-package, everything else flat
- This is the **cleanest** aggregate package. Only 2 files at root level (aggregate root + repository), events in sub-package.
- No entities or value objects inside (the stock fields are primitives).
- No PageSlice DTO pollution.

---

## 5. `member/` Package — 3 files, FLAT (no sub-packages)

### Classes:
| File | Type | DDD Role | Notes |
|------|------|----------|-------|
| `Member.java` | Class | **Aggregate Root** | `extends AggregateRoot`. Has `address: Address` (value object composition). |
| `Address.java` | Class | **Value Object** | Immutable (`final` fields, `@EqualsAndHashCode`). But does NOT extend `ValueObject` base class. |
| `MemberRepository.java` | Interface | **Repository** | Includes `countTotal()` — a query concern leaking into domain repo. |

### Organization: Completely flat, NO event sub-package
- Unlike order/product/inventory, **no events sub-package** — Member has no domain events at all.
- Value object (Address) is mixed flat with aggregate root and repository.
- `Address` duplicates much of the structure of `ShippingAddress` in order (province, city, district, detail + `fullAddress()`).

### Anomalies:
- `Address` vs `ShippingAddress` — two nearly identical value objects in different aggregates. Could be a shared value object in `shared/` or at least one should reference the other.
- `countTotal()` in repository is a read concern.

---

## 6. `cart/` Package — 3 files, FLAT (no sub-packages)

### Classes:
| File | Type | DDD Role | Notes |
|------|------|----------|-------|
| `Cart.java` | Class | **Aggregate Root** | `extends AggregateRoot`. Composes `List<CartItem>` (entities). Uses `BigDecimal` directly instead of `Money`. |
| `CartItem.java` | Class | **Entity** | `extends Entity`. Uses `BigDecimal` for price instead of `Money` value object. |
| `CartRepository.java` | Interface | **Repository** | Clean: only `findByMemberId` and `save`. |

### Organization: Completely flat, NO event sub-package
- Like member: no events sub-package, everything flat.
- **No domain events** for Cart at all.

### Anomalies:
- **`CartItem` uses `BigDecimal` for `unitPrice`** instead of the `Money` value object defined in `shared/`. This is inconsistent with `OrderItem` which uses `Money`. The domain already has a `Money` value object — Cart should use it.
- `Cart.addItem()` method signature uses `java.math.BigDecimal` as a raw parameter, bypassing domain value objects.

---

## 7. `shared/` Package — 7 files, FLAT (no sub-packages)

### Classes:
| File | Type | DDD Role | Notes |
|------|------|----------|-------|
| `AggregateRoot.java` | Abstract Class | **Base Class** | `extends Entity`. Adds `version` (optimistic locking) + `domainEvents` list + `registerEvent()`/`clearDomainEvents()`. |
| `Entity.java` | Abstract Class | **Base Class** | ID-based equality. Abstract base for all entities. |
| `ValueObject.java` | Abstract Class | **Base Class** | Value-based equality contract (abstract `equals`/`hashCode`). |
| `DomainEvent.java` | Interface | **Base Interface** | Marker for all domain events. Has `occurredOn()` method. |
| `DomainException.java` | Class | **Domain Exception** | Business rule violation exception. |
| `Money.java` | Class | **Shared Value Object** | Immutable. Extends nothing (does NOT extend `ValueObject`). Has arithmetic operations (add, subtract, multiply). |
| `TokenService.java` | Interface | **Domain Service Interface** | JWT token generation. This is a **domain service port** (dependency inversion). |

### Organization: Completely flat
- All base abstractions + shared value objects + domain service interface mixed together.
- No sub-packages for `base/`, `valueobject/`, `service/`, `exception/`.

### Anomalies & Issues:
1. **`Money` does NOT extend `ValueObject`** — it's a value object by semantics (immutable, equality-by-value) but doesn't extend the `ValueObject` base class that exists in the same package. It uses Lombok `@EqualsAndHashCode` instead.
2. **`TokenService` in the domain layer** — This is a domain service port, which is legitimate in DDD (dependency inversion). However, its methods (`generateAdminToken`, `generateMemberToken`) are **authentication/infrastructure concerns** that arguably don't belong in the domain core. It creates a dependency from domain → auth concepts.
3. **`ValueObject` base class is never actually used** — None of the concrete value objects (`Money`, `ShippingAddress`, `Address`, `Permission`) extend it. They all use Lombok `@EqualsAndHashCode` instead. The base class is effectively dead code.
4. **No shared events** — `DomainEvent` interface is in shared, but concrete events are per-aggregate (correct). However, there are no cross-aggregate integration events.

---

## Cross-Package Comparison Matrix

| Package | Files | Aggregates | Entities | Value Objects | Enums | Events | Repositories | PageSlice DTOs | Sub-packages |
|---------|-------|-----------|----------|---------------|-------|--------|-------------|----------------|--------------|
| admin   | 10    | 3 (Admin, Role, Menu) | 0 | 1 (Permission) | 3 | 0 | 3 | 0 | None |
| product | 7     | 1 (Product) | 1 (ProductSku) | 0 | 1 | 2 | 1 | 1 | `event/` |
| order   | 9     | 1 (Order) | 1 (OrderItem) | 1 (ShippingAddress) | 1 | 3 | 1 | 1 | `event/` |
| inventory | 4   | 1 (Inventory) | 0 | 0 | 0 | 2 | 1 | 0 | `event/` |
| member  | 3     | 1 (Member) | 0 | 1 (Address) | 0 | 0 | 1 | 0 | None |
| cart    | 3     | 1 (Cart) | 1 (CartItem) | 0 | 0 | 0 | 1 | 0 | None |
| shared  | 7     | 0 | 0 | 1 (Money) + 2 base | 0 | 0 (interface only) | 0 | 0 | None |

---

## Key Inconsistencies

### 1. Event Organization Inconsistency
- **product, order, inventory**: Events in `event/` sub-package ✅
- **admin, member, cart**: No events at all (admin has 3 aggregates but 0 events; member and cart have 0 events) ⚠️

### 2. Value Object Base Class Inconsistency
- `shared/ValueObject` abstract base exists but is **never used** by any concrete value object
- `Money`, `ShippingAddress`, `Address`, `Permission` all use Lombok `@EqualsAndHashCode` instead of extending `ValueObject`

### 3. Money vs BigDecimal Inconsistency
- `OrderItem.unitPrice` → uses `Money` ✅
- `ProductSku.price` → uses `Money` ✅
- `CartItem.unitPrice` → uses **raw `BigDecimal`** ❌ (inconsistent)
- `Cart.addItem()` → takes `BigDecimal` parameter ❌

### 4. PageSlice DTOs in Domain Layer
- `OrderPageSlice` and `ProductPageSlice` are **read-model DTOs** that belong to the application/query layer, NOT the domain. They leak query concerns into the domain.

### 5. Repository Interface Pollution
- `OrderRepository` has admin-specific methods (`findPageForAdmin`, `countTotal`, `countByCreatedAtSince`, `sumTotalAmountSince`) — these are application-side queries, not domain repository concerns
- `ProductRepository` has `findPage` with filter parameters
- `MemberRepository` has `countTotal()`
- `InventoryRepository` and `CartRepository` are clean ✅

### 6. Admin Package: Multi-Aggregate Violation
- `admin/` contains **3 distinct aggregate roots** (Admin, Role, Menu) with **3 separate repositories** — all in one flat package
- Per DDD, each aggregate should have its own bounded context / package boundary
- `AdminStatus` enum is shared between `Admin` and `Role` aggregates — another cross-aggregate coupling
- `Permission` value object belongs to Role's bounded context but sits in the shared `admin/` package

### 7. Address Duplication
- `member/Address` and `order/ShippingAddress` are **nearly identical value objects** (province, city, district, detail, `fullAddress()`)
- `ShippingAddress` adds `receiverName`, `receiverPhone` — but the address portion is identical
- Should be a shared value object or ShippingAddress should compose Address

### 8. TokenService Placement
- `TokenService` is a domain service port (interface) but its semantics are **purely infrastructure/auth** (JWT generation)
- It arguably belongs in an application service port package, not the domain core
- It creates tight coupling: domain layer now knows about "admin token" and "member token" concepts

---

## Proposed Refactoring Directions

### A. Split `admin/` into separate aggregate packages
```
admin/
  adminaggregate/
    Admin.java          (aggregate root)
    AdminStatus.java    (enum)
    AdminRepository.java (repository)
  roleaggregate/
    Role.java           (aggregate root)
    Permission.java     (value object)
    PermissionType.java (enum)
    RoleRepository.java (repository)
  menuaggregate/
    Menu.java           (aggregate root)
    MenuType.java       (enum)
    MenuRepository.java (repository)
```
Or use a simpler naming convention:
```
admin/
  Admin.java, AdminRepository.java, AdminStatus.java
  role/
    Role.java, RoleRepository.java, Permission.java, PermissionType.java
  menu/
    Menu.java, MenuRepository.java, MenuType.java
```

### B. Consistent sub-package structure for all aggregates
Each aggregate package should have consistent sub-packages:
```
{aggregate}/
  {AggregateRoot}.java
  entity/
    {Entity}.java
  valueobject/
    {ValueObject}.java
  event/
    {Event}.java
  repository/
    {Repository}.java
```

### C. Remove PageSlice DTOs from domain
Move `OrderPageSlice` and `ProductPageSlice` to the application layer.

### D. Clean up repository interfaces
Split read/query methods out of domain repositories into separate query interfaces in the application layer.

### E. Fix Money inconsistency in Cart
Replace `BigDecimal` with `Money` value object in `CartItem` and `Cart`.

### F. Fix ValueObject base class usage
Either:
- Make `Money`, `ShippingAddress`, `Address`, `Permission` extend `ValueObject`
- OR remove the unused `ValueObject` base class (since Lombok handles it)

### G. Unify Address value objects
Create a shared `Address` value object and let `ShippingAddress` compose it with `receiverName`/`receiverPhone`.

### H. Relocate TokenService
Move `TokenService` to an application-layer port package or a dedicated auth domain module.