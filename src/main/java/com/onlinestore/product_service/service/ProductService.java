package com.onlinestore.product_service.service;

import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.exception.ProductOptionGroupNotFoundException;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductOption;
import com.onlinestore.product_service.model.ProductOptionGroup;
import com.onlinestore.product_service.model.ProductType;
import com.onlinestore.product_service.repository.ProductOptionGroupRepository;
import com.onlinestore.product_service.repository.ProductOptionRepository;
import com.onlinestore.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;

    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findAllByActiveTrue(pageable);
    }

    public Page<Product> getActiveProductsByType(ProductType type, Pageable pageable) {
        return productRepository.findAllByTypeAndActiveTrue(type, pageable);
    }

    public Product getActiveProductById(Long id) {
        return findActiveProductOrThrow(id);
    }

    public List<ProductOptionGroup> getActiveOptionGroups(Long productId) {
        findActiveProductOrThrow(productId);
        return productOptionGroupRepository.findAllByProductIdAndActiveTrue(productId);
    }

    public List<ProductOption> getActiveOptions(Long groupId) {
        ProductOptionGroup group = productOptionGroupRepository.findByIdAndActiveTrue(groupId)
                .orElseThrow(() -> new ProductOptionGroupNotFoundException(groupId));
        findActiveProductOrThrow(group.getProduct().getId());
        return productOptionRepository.findAllByGroupIdAndActiveTrue(groupId);
    }

    private Product findActiveProductOrThrow(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
