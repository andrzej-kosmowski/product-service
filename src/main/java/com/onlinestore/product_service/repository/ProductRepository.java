package com.onlinestore.product_service.repository;

import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByActiveTrue(Pageable pageable);
    Page<Product> findAllByTypeAndActiveTrue(ProductType type, Pageable pageable);
    Optional<Product> findByIdAndActiveTrue(Long id);
}
