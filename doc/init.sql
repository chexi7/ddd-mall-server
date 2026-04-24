-- ============================================
-- DDD Mall 数据库初始化脚本
-- 数据库：mall
-- 字符集：utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mall;

-- ============================================
-- 商品聚合
-- ============================================

CREATE TABLE IF NOT EXISTS products (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version         BIGINT DEFAULT 0,
    name            VARCHAR(200) NOT NULL COMMENT '商品名称',
    description     VARCHAR(2000) COMMENT '商品描述',
    price           DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    status          VARCHAR(20) NOT NULL COMMENT '商品状态: DRAFT/ON_SALE/OFF_SALE',
    category        VARCHAR(100) NOT NULL COMMENT '商品分类',
    created_at      DATETIME NOT NULL COMMENT '创建时间',
    updated_at      DATETIME COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS product_skus (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT NOT NULL COMMENT '所属商品ID',
    name            VARCHAR(200) NOT NULL COMMENT 'SKU名称',
    price           DECIMAL(10,2) NOT NULL COMMENT 'SKU价格',
    attributes      VARCHAR(500) COMMENT 'SKU属性(JSON)',
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- ============================================
-- 库存聚合
-- ============================================

CREATE TABLE IF NOT EXISTS inventories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version         BIGINT DEFAULT 0,
    product_id      BIGINT NOT NULL COMMENT '商品ID',
    total_stock     INT NOT NULL DEFAULT 0 COMMENT '总库存',
    available_stock INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    locked_stock    INT NOT NULL DEFAULT 0 COMMENT '锁定库存',
    UNIQUE KEY uk_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- ============================================
-- 购物车聚合
-- ============================================

CREATE TABLE IF NOT EXISTS carts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version         BIGINT DEFAULT 0,
    member_id       BIGINT NOT NULL COMMENT '会员ID',
    UNIQUE KEY uk_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

CREATE TABLE IF NOT EXISTS cart_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id         BIGINT NOT NULL COMMENT '购物车ID',
    product_id      BIGINT NOT NULL COMMENT '商品ID',
    sku_id          BIGINT COMMENT 'SKU ID',
    product_name    VARCHAR(200) NOT NULL COMMENT '商品名称(冗余)',
    quantity        INT NOT NULL COMMENT '数量',
    unit_price      DECIMAL(10,2) NOT NULL COMMENT '单价',
    INDEX idx_cart_id (cart_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车项表';

-- ============================================
-- 订单聚合
-- ============================================

CREATE TABLE IF NOT EXISTS orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    version             BIGINT DEFAULT 0,
    order_no            VARCHAR(32) NOT NULL COMMENT '订单号',
    member_id           BIGINT NOT NULL COMMENT '会员ID',
    total_amount        DECIMAL(12,2) NOT NULL COMMENT '订单总额',
    status              VARCHAR(20) NOT NULL COMMENT '订单状态: PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CANCELLED',
    receiver_name       VARCHAR(50) COMMENT '收货人',
    receiver_phone      VARCHAR(20) COMMENT '收货电话',
    shipping_province   VARCHAR(20) COMMENT '省',
    shipping_city       VARCHAR(20) COMMENT '市',
    shipping_district   VARCHAR(20) COMMENT '区',
    shipping_detail     VARCHAR(200) COMMENT '详细地址',
    created_at          DATETIME NOT NULL COMMENT '创建时间',
    paid_at             DATETIME COMMENT '支付时间',
    shipped_at          DATETIME COMMENT '发货时间',
    completed_at        DATETIME COMMENT '完成时间',
    cancelled_at        DATETIME COMMENT '取消时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_member_id (member_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS order_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL COMMENT '订单ID',
    product_id      BIGINT NOT NULL COMMENT '商品ID',
    sku_id          BIGINT COMMENT 'SKU ID',
    product_name    VARCHAR(200) NOT NULL COMMENT '商品名称(快照)',
    unit_price      DECIMAL(10,2) NOT NULL COMMENT '单价(快照)',
    quantity        INT NOT NULL COMMENT '数量',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- ============================================
-- 会员聚合（C端用户）
-- ============================================

CREATE TABLE IF NOT EXISTS members (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version         BIGINT DEFAULT 0,
    username        VARCHAR(50) NOT NULL COMMENT '用户名',
    password        VARCHAR(100) NOT NULL COMMENT '密码',
    nickname        VARCHAR(50) COMMENT '昵称',
    phone           VARCHAR(20) COMMENT '手机号',
    province        VARCHAR(20) COMMENT '省',
    city            VARCHAR(20) COMMENT '市',
    district        VARCHAR(20) COMMENT '区',
    address_detail  VARCHAR(200) COMMENT '详细地址',
    zip_code        VARCHAR(10) COMMENT '邮编',
    created_at      DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- ============================================
-- 后台管理聚合（RBAC）
-- ============================================

CREATE TABLE IF NOT EXISTS admins (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version         BIGINT DEFAULT 0,
    username        VARCHAR(50) NOT NULL COMMENT '用户名',
    password        VARCHAR(100) NOT NULL COMMENT '密码',
    real_name       VARCHAR(50) COMMENT '真实姓名',
    phone           VARCHAR(20) COMMENT '手机号',
    email           VARCHAR(100) COMMENT '邮箱',
    status          VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED/DISABLED',
    created_at      DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

CREATE TABLE IF NOT EXISTS roles (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version         BIGINT DEFAULT 0,
    name            VARCHAR(50) NOT NULL COMMENT '角色名称',
    code            VARCHAR(50) NOT NULL COMMENT '角色编码',
    description     VARCHAR(200) COMMENT '角色描述',
    status          VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED/DISABLED',
    created_at      DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS admin_roles (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id        BIGINT NOT NULL COMMENT '管理员ID',
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    INDEX idx_admin_id (admin_id),
    INDEX idx_role_id (role_id),
    UNIQUE KEY uk_admin_role (admin_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员-角色关联表';

CREATE TABLE IF NOT EXISTS role_permissions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限标识，如 product:create',
    INDEX idx_role_id (role_id),
    UNIQUE KEY uk_role_permission (role_id, permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

CREATE TABLE IF NOT EXISTS menus (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version         BIGINT DEFAULT 0,
    name            VARCHAR(100) NOT NULL COMMENT '菜单名称',
    parent_id       BIGINT DEFAULT 0 COMMENT '父菜单ID，0为顶级',
    path            VARCHAR(200) COMMENT '路由路径',
    component       VARCHAR(200) COMMENT '前端组件路径',
    icon            VARCHAR(50) COMMENT '图标',
    permission_code VARCHAR(100) COMMENT '权限标识',
    type            VARCHAR(20) NOT NULL COMMENT '类型: DIRECTORY/MENU/BUTTON',
    sort            INT NOT NULL DEFAULT 0 COMMENT '排序',
    visible         TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可见',
    created_at      DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ============================================
-- 初始化数据：超级管理员 + 角色
-- ============================================

-- 超级管理员（密码: admin123）
INSERT INTO admins (username, password, real_name, status, created_at)
VALUES ('admin', 'admin123', '超级管理员', 'ENABLED', NOW());

-- 超级管理员角色
INSERT INTO roles (name, code, description, status, created_at)
VALUES ('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 'ENABLED', NOW());

-- 绑定角色
INSERT INTO admin_roles (admin_id, role_id) VALUES (1, 1);

-- 超管拥有所有权限
INSERT INTO role_permissions (role_id, permission_code) VALUES
(1, 'admin:create'),
(1, 'admin:update'),
(1, 'admin:delete'),
(1, 'admin:assign-role'),
(1, 'role:create'),
(1, 'role:update'),
(1, 'role:delete'),
(1, 'role:assign-permission'),
(1, 'menu:create'),
(1, 'menu:update'),
(1, 'menu:delete'),
(1, 'product:create'),
(1, 'product:update'),
(1, 'product:delete'),
(1, 'product:publish'),
(1, 'order:view'),
(1, 'order:ship'),
(1, 'member:view'),
(1, 'member:update');

-- 初始菜单
INSERT INTO menus (name, parent_id, path, component, icon, permission_code, type, sort, visible, created_at) VALUES
-- 顶级目录
('系统管理', 0, '/system', NULL, 'setting', NULL, 'DIRECTORY', 1, 1, NOW()),
('商品管理', 0, '/product', NULL, 'shopping', NULL, 'DIRECTORY', 2, 1, NOW()),
('订单管理', 0, '/order', NULL, 'document', NULL, 'DIRECTORY', 3, 1, NOW()),
('会员管理', 0, '/member', NULL, 'user', NULL, 'DIRECTORY', 4, 1, NOW()),

-- 系统管理 - 子菜单
('管理员管理', 1, '/system/admin', 'system/admin/index', 'people', 'admin:view', 'MENU', 1, 1, NOW()),
('角色管理', 1, '/system/role', 'system/role/index', 'key', 'role:view', 'MENU', 2, 1, NOW()),
('菜单管理', 1, '/system/menu', 'system/menu/index', 'menu', 'menu:view', 'MENU', 3, 1, NOW()),

-- 商品管理 - 子菜单
('商品列表', 2, '/product/list', 'product/list/index', 'list', 'product:view', 'MENU', 1, 1, NOW()),

-- 订单管理 - 子菜单
('订单列表', 3, '/order/list', 'order/list/index', 'list', 'order:view', 'MENU', 1, 1, NOW()),

-- 会员管理 - 子菜单
('会员列表', 4, '/member/list', 'member/list/index', 'list', 'member:view', 'MENU', 1, 1, NOW());
