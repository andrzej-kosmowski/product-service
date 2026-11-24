package com.onlinestore.product_service.service;

import com.onlinestore.product_service.command.CreateProductCommand;
import com.onlinestore.product_service.command.UpdateProductCommand;
import com.onlinestore.product_service.dto.ProductConfigurationDto;
import com.onlinestore.product_service.dto.ProductDto;
import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.dto.ProductOptionGroupDto;
import com.onlinestore.product_service.exception.ProductAlreadyExistsException;
import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.exception.ProductOptionGroupNotFoundException;
import com.onlinestore.product_service.mapper.ProductMapper;
import com.onlinestore.product_service.mapper.ProductOptionGroupMapper;
import com.onlinestore.product_service.mapper.ProductOptionMapper;
import com.onlinestore.product_service.model.Product;
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
    private final ProductMapper productMapper;
    private final ProductOptionGroupMapper productOptionGroupMapper;
    private final ProductOptionMapper productOptionMapper;

    public Page<ProductDto> getActiveProducts(Pageable pageable) {
        return productRepository.findAllByActiveTrue(pageable)
                .map(productMapper::toDto);
    }

    public Page<ProductDto> getActiveProductsByType(ProductType type, Pageable pageable) {
        return productRepository.findAllByTypeAndActiveTrue(type, pageable)
                .map(productMapper::toDto);
    }

    public ProductDto getActiveProductById(Long id) {
        Product product = findActiveProductOrThrow(id);
        return productMapper.toDto(product);
    }

    public ProductConfigurationDto getProductConfiguration(Long productId) {
        findActiveProductOrThrow(productId);
        List<ProductOptionGroupDto> groups = productOptionGroupRepository
                .findAllByProductIdAndActiveTrue(productId)
                .stream()
                .map(group -> {
                    List<ProductOptionDto> options = productOptionRepository
                            .findAllByGroupIdAndActiveTrue(group.getId())
                            .stream()
                            .map(productOptionMapper::toDto)
                            .toList();
                    return productOptionGroupMapper.toDtoWithOptions(group, options);
                })
                .toList();
        return new ProductConfigurationDto(productId, groups);
    }

    @Transactional
    public ProductDto createProduct(CreateProductCommand command) {
        if (productRepository.existsBySku(command.sku())) {
            throw new ProductAlreadyExistsException(command.sku());
        }
        Product product = Product.create(
                command.sku(),
                command.name(),
                command.description(),
                command.netPrice(),
                command.taxRate(),
                command.type()
        );
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductCommand command) {
        Product product = findActiveProductOrThrow(id);
        if (productRepository.existsBySkuAndIdNot(command.sku(), id)) {
            throw new ProductAlreadyExistsException(command.sku());
        }
        product.update(command.sku(), command.name(),
                command.description(), command.netPrice(), command.taxRate(), command.type());
        return productMapper.toDto(product);
    }

    @Transactional
    public void activateProduct(Long id) {
        Product product = findProductOrThrow(id);
        product.activate();
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = findProductOrThrow(id);
        product.deactivate();
    }

    private Product findActiveProductOrThrow(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
