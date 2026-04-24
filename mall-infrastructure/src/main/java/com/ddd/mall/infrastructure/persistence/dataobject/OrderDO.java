package com.ddd.mall.infrastructure.persistence.dataobject;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(nullable = false)
    private Long memberId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private List<OrderItemDO> items = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 20)
    private String status;

    // 收货地址（扁平化存储）
    @Column(length = 50)
    private String receiverName;
    @Column(length = 20)
    private String receiverPhone;
    @Column(length = 20)
    private String shippingProvince;
    @Column(length = 20)
    private String shippingCity;
    @Column(length = 20)
    private String shippingDistrict;
    @Column(length = 200)
    private String shippingDetail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
}
