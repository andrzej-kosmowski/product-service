package com.onlinestore.product_service.service;

import com.onlinestore.product_service.command.CreateProductCommand;
import com.onlinestore.product_service.command.UpdateProductCommand;
import com.onlinestore.product_service.dto.*;
import com.onlinestore.product_service.exception.InvalidProductOptionGroupException;
import com.onlinestore.product_service.exception.ProductAlreadyExistsException;
import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.mapper.ProductMapper;
import com.onlinestore.product_service.mapper.ProductOptionGroupMapper;
import com.onlinestore.product_service.mapper.ProductOptionMapper;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductOption;
import com.onlinestore.product_service.model.ProductOptionGroup;
import com.onlinestore.product_service.model.ProductType;
import com.onlinestore.product_service.repository.ProductOptionGroupRepository;
import com.onlinestore.product_service.repository.ProductOptionRepository;
import com.onlinestore.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductMapper productMapper;
    private final ProductOptionGroupMapper productOptionGroupMapper;
    private final ProductOptionMapper productOptionMapper;

    public PageResponse<ProductDto> getActiveProducts(Pageable pageable) {
        log.info("Fetching active products. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findAllByActiveTrue(pageable);
        Page<ProductDto> productDtos = products.map(productMapper::toDto);
        log.info("Fetched {} active products", productDtos.getNumberOfElements());
        return PageResponse.from(productDtos);
    }

    public PageResponse<ProductDto> getActiveProductsByType(ProductType type, Pageable pageable) {
        log.info("Fetching active products of type {}. Page: {}, Size: {}",
                type, pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findAllByTypeAndActiveTrue(type, pageable);
        Page<ProductDto> productDtos = products.map(productMapper::toDto);
        log.info("Fetched {} active products of type {}", productDtos.getNumberOfElements(), type);
        return PageResponse.from(productDtos);
    }

    public ProductDto getActiveProductById(Long id) {
        log.info("Fetching active product with id {}", id);
        Product product = findActiveProductOrThrow(id);
        return productMapper.toDto(product);
    }

    public ProductConfigurationDto getProductConfiguration(Long productId) {
        log.info("Fetching configuration for product with id {}", productId);
        findActiveProductOrThrow(productId);

        List<ProductOptionGroup> optionGroups = productOptionGroupRepository
                .findAllByProductIdAndActiveTrue(productId);

        if (optionGroups.isEmpty()) {
            return new ProductConfigurationDto(productId, List.of());
        }

        List<Long> groupIds = optionGroups.stream()
                .map(ProductOptionGroup::getId)
                .toList();

        Map<Long, List<ProductOptionDto>> optionsByGroupId = productOptionRepository
                .findAllByGroupIdInAndActiveTrue(groupIds)
                .stream()
                .filter(this::isAvailableOption)
                .collect(Collectors.groupingBy(
                        option -> option.getGroup().getId(),
                        Collectors.mapping(
                                productOptionMapper::toDto,
                                Collectors.toList()
                        )
                ));
        List<ProductOptionGroupDto> groups = optionGroups.stream()
                .map(group -> productOptionGroupMapper.toDtoWithOptions(
                        group,
                        optionsByGroupId.getOrDefault(group.getId(), List.of())
                ))
                .toList();
        return new ProductConfigurationDto(productId, groups);
    }


    @Transactional
    public ProductDto createProduct(CreateProductCommand command) {
        log.info("Creating product with SKU {} and type {}", command.sku(), command.type());
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
        log.info("Created product with id {}", savedProduct.getId());
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductCommand command) {
        log.info("Updating product with id {}", id);
        Product product = findActiveProductOrThrow(id);
        if (productRepository.existsBySkuAndIdNot(command.sku(), id)) {
            throw new ProductAlreadyExistsException(command.sku());
        }
        if (product.getType() != ProductType.ELECTRONICS
                && command.type() == ProductType.ELECTRONICS
                && productOptionGroupRepository.existsByProductIdAndActiveTrue(id)) {
            throw new InvalidProductOptionGroupException(
                    "A product with active option groups cannot be changed to ELECTRONICS"
            );
        }
        product.update(command.sku(), command.name(),
                command.description(), command.netPrice(), command.taxRate(), command.type());
        log.info("Updated product with id {}", id);
        return productMapper.toDto(product);
    }

    @Transactional
    public void activateProduct(Long id) {
        log.info("Activating product with id {}", id);
        Product product = findProductOrThrow(id);
        product.activate();
        log.info("Activated product with id {}", id);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        log.info("Deactivating product with id {}", id);
        Product product = findProductOrThrow(id);
        product.deactivate();
        log.info("Deactivated product with id {}", id);
    }

    private Product findActiveProductOrThrow(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private boolean isAvailableOption(ProductOption option) {
        Product optionProduct = option.getOptionProduct();
        return optionProduct == null || Boolean.TRUE.equals(optionProduct.getActive());
    }

}
