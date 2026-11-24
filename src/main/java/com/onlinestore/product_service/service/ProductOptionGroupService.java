package com.onlinestore.product_service.service;

import com.onlinestore.product_service.command.CreateProductOptionGroupCommand;
import com.onlinestore.product_service.command.UpdateProductOptionGroupCommand;
import com.onlinestore.product_service.dto.ProductOptionGroupDto;
import com.onlinestore.product_service.exception.InvalidProductOptionGroupException;
import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.exception.ProductOptionGroupAlreadyExistsException;
import com.onlinestore.product_service.exception.ProductOptionGroupNotFoundException;
import com.onlinestore.product_service.mapper.ProductOptionGroupMapper;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductOptionGroup;
import com.onlinestore.product_service.model.ProductOption;
import com.onlinestore.product_service.model.ProductType;
import com.onlinestore.product_service.repository.ProductOptionGroupRepository;
import com.onlinestore.product_service.repository.ProductOptionRepository;
import com.onlinestore.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductOptionGroupService {
    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionGroupMapper productOptionGroupMapper;

    public List<ProductOptionGroupDto> getActiveOptionGroups(Long productId) {
        log.info("Fetching active option groups for product with id {}", productId);
        findActiveProductOrThrow(productId);
        List<ProductOptionGroupDto> groups = productOptionGroupRepository
                .findAllByProductIdAndActiveTrue(productId)
                .stream()
                .map(productOptionGroupMapper::toDto)
                .toList();
        log.info("Fetched {} active option groups for product with id {}", groups.size(), productId);
        return groups;
    }

    @Transactional
    public ProductOptionGroupDto createProductOptionGroup(Long productId, CreateProductOptionGroupCommand command) {
        log.info("Creating option group with code {} for product with id {}", command.code(), productId);
        Product product = findActiveProductOrThrow(productId);
        if (product.getType() == ProductType.ELECTRONICS) {
            throw new InvalidProductOptionGroupException(
                    "Products of type ELECTRONICS cannot have option groups"
            );
        }
        if (productOptionGroupRepository.existsByProductIdAndCode(productId, command.code())) {
            throw new ProductOptionGroupAlreadyExistsException(productId, command.code());
        }
        ProductOptionGroup group = productOptionGroupMapper.from(command);
        group.setProduct(product);
        group.setActive(true);
        group.validateConfiguration();
        ProductOptionGroup savedGroup = productOptionGroupRepository.save(group);
        log.info("Created option group with id {} for product with id {}", savedGroup.getId(), productId);
        return productOptionGroupMapper.toDto(savedGroup);
    }

    @Transactional
    public ProductOptionGroupDto updateOptionGroup(Long groupId, UpdateProductOptionGroupCommand command) {
        log.info("Updating option group with id {}", groupId);
        ProductOptionGroup group = productOptionGroupRepository.findByIdAndActiveTrue(groupId)
                .orElseThrow(() -> new ProductOptionGroupNotFoundException(groupId));
        Long productId = group.getProduct().getId();
        findActiveProductOrThrow(productId);
        if (productOptionGroupRepository.existsByProductIdAndCodeAndIdNot(productId, command.code(), groupId)) {
            throw new ProductOptionGroupAlreadyExistsException(productId, command.code());
        }
        group.update(command.name(), command.code(), command.selectionType(), command.required(),
                command.minSelections(), command.maxSelections());
        log.info("Updated option group with id {}", groupId);
        return productOptionGroupMapper.toDto(group);
    }

    @Transactional
    public void deactivateOptionGroup(Long groupId) {
        log.info("Deactivating option group with id {} and its active options", groupId);
        ProductOptionGroup group = findOptionGroupOrThrow(groupId);
        group.deactivate();

        productOptionRepository.findAllByGroupIdAndActiveTrue(groupId)
                .forEach(ProductOption::deactivate);
        log.info("Deactivated option group with id {} and its active options", groupId);
    }

    @Transactional
    public void activateOptionGroup(Long groupId) {
        log.info("Activating option group with id {}", groupId);
        ProductOptionGroup group = findOptionGroupOrThrow(groupId);
        findActiveProductOrThrow(group.getProduct().getId());
        group.activate();
        log.info("Activated option group with id {}", groupId);
    }

    private ProductOptionGroup findOptionGroupOrThrow(Long id) {
        return productOptionGroupRepository.findById(id)
                .orElseThrow(() -> new ProductOptionGroupNotFoundException(id));
    }

    private Product findActiveProductOrThrow(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
