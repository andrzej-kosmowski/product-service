package com.onlinestore.product_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(name = "net_price", nullable = false)
    private BigDecimal netPrice;
    @Column(name = "gross_price", nullable = false)
    private BigDecimal grossPrice;
    @Column(name = "tax_rate", nullable = false)
    private BigDecimal taxRate;
    @Enumerated(EnumType.STRING)
    private ProductType type;
    @Column(nullable = false)
    private Boolean active;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "product")
    private List<ProductOptionGroup> optionGroups = new ArrayList<>();
}
