package com.onlinestore.product_service.model;

import com.onlinestore.product_service.exception.InvalidProductOptionException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_option")
@Getter
@Setter
@NoArgsConstructor
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50)
    private String code;
    @Column(length = 150)
    private String value;
    @Column(nullable = false)
    private Boolean active;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ProductOptionGroup group;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_product_id")
    private Product optionProduct;

    public void update(
            String name,
            String code,
            String value,
            Product optionProduct
    ) {
        validate(value, optionProduct);
        this.name = name;
        this.code = code;
        this.value = value;
        this.optionProduct = optionProduct;
    }

    public void initialize() {
        this.active = true;
        validate();
    }

    public void validate() {
        validate(value, optionProduct);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private void validate(String value, Product optionProduct) {
        boolean hasValue = value != null && !value.isBlank();
        boolean hasOptionProduct = optionProduct != null;

        if (hasValue == hasOptionProduct) {
            throw new InvalidProductOptionException(
                    "Product option must have either a value or an option product, but not both"
            );
        }
    }
}
