package com.onlinestore.product_service.repository;

import com.onlinestore.product_service.model.ProductOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOptionGroupRepository extends JpaRepository<ProductOptionGroup, Long> {
    List<ProductOptionGroup> findAllByProductIdAndActiveTrue(Long productId);
    Optional<ProductOptionGroup> findByIdAndActiveTrue(Long id);
    boolean existsByProductIdAndCode(Long productId, String code);
    boolean existsByProductIdAndCodeAndIdNot(Long productId, String code, Long id);
}
