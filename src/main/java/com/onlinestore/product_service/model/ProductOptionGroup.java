package com.onlinestore.product_service.model;

import com.onlinestore.product_service.exception.InvalidProductOptionGroupException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_option_group")
@Getter
@Setter
@NoArgsConstructor
public class ProductOptionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50)
    private String code;
    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false)
    private SelectionType selectionType;
    @Column(nullable = false)
    private Boolean required = false;
    @Column(name = "min_selections", nullable = false)
    private Integer minSelections = 0;
    @Column(name = "max_selections")
    private Integer maxSelections;
    @Column(nullable = false)
    private Boolean active;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @OneToMany(mappedBy = "group")
    private List<ProductOption> options = new ArrayList<>();

    public void update(
            String name,
            String code,
            SelectionType selectionType,
            Boolean required,
            Integer minSelections,
            Integer maxSelections
    ) {
        this.name = name;
        this.code = code;
        this.selectionType = selectionType;
        this.required = required;
        this.minSelections = minSelections;
        this.maxSelections = maxSelections;

        validateConfiguration();
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void validateConfiguration() {
        if (required && minSelections < 1) {
            throw new InvalidProductOptionGroupException("Required option group must have at least one minimum selection");
        }
        if (!required && minSelections != 0) {
            throw new InvalidProductOptionGroupException("Optional option group must have zero minimum selections");
        }
        if (maxSelections != null && maxSelections < minSelections) {
            throw new InvalidProductOptionGroupException("Maximum selections cannot be lower than minimum selections");
        }
        if (selectionType == SelectionType.SINGLE
                && (minSelections > 1 || (maxSelections != null && maxSelections > 1))) {
            throw new InvalidProductOptionGroupException(
                    "Single selection group cannot have more than one maximum selection");
        }
    }
}
