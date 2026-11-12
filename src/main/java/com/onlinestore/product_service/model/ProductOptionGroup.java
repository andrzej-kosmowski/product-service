package com.onlinestore.product_service.model;

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
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String code;
    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false)
    private SelectionType selectionType;
    @Column(nullable = false)
    private Boolean required = false;
    @Column(name = "min_selections", nullable = false)
    private Integer minSelections = 0;
    @Column(name = "max_selections", nullable = false)
    private Integer maxSelections;
    @Column(nullable = false)
    private Boolean active;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @OneToMany(mappedBy = "group")
    private List<ProductOption> options = new ArrayList<>();

}
