package com.onlinestore.product_service.repository;

import com.onlinestore.product_service.model.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    List<ProductOption> findAllByGroupIdAndActiveTrue(Long groupId);
    Optional<ProductOption> findByIdAndActiveTrue(Long id);
}
