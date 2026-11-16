package com.onlinestore.product_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Column(nullable = false, unique = true, length = 50)
    private String sku;
    @Column(nullable = false, length = 150)
    private String name;
    private String description;
    @Column(name = "net_price", nullable = false)
    private BigDecimal netPrice;
    @Column(name = "gross_price", nullable = false)
    private BigDecimal grossPrice;
    @Column(name = "tax_rate", nullable = false)
    private BigDecimal taxRate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;
    @Column(nullable = false)
    private Boolean active;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "product")
    private List<ProductOptionGroup> optionGroups = new ArrayList<>();

    public static Product create(
            String sku,
            String name,
            String description,
            BigDecimal netPrice,
            BigDecimal taxRate,
            ProductType type
    ) {
        Product product = new Product();

        product.sku = sku;
        product.name = name;
        product.description = description;
        product.netPrice = netPrice;
        product.taxRate = taxRate;
        product.grossPrice = calculateGrossPrice(netPrice, taxRate);
        product.type = type;
        product.active = true;

        return product;
    }

    public void update(
            String sku,
            String name,
            String description,
            BigDecimal netPrice,
            BigDecimal taxRate,
            ProductType type
    ) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.netPrice = netPrice;
        this.grossPrice = calculateGrossPrice(netPrice, taxRate);
        this.taxRate = taxRate;
        this.type = type;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private static BigDecimal calculateGrossPrice(BigDecimal netPrice, BigDecimal taxRate) {
        return netPrice
                .multiply(BigDecimal.ONE.add(taxRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
