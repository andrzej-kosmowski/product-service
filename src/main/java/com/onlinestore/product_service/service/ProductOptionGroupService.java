package com.onlinestore.product_service.service;

import com.onlinestore.product_service.command.CreateProductOptionGroupCommand;
import com.onlinestore.product_service.command.UpdateProductOptionGroupCommand;
import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.dto.ProductOptionGroupDto;
import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.exception.ProductOptionGroupAlreadyExistsException;
import com.onlinestore.product_service.exception.ProductOptionGroupNotFoundException;
import com.onlinestore.product_service.mapper.ProductOptionGroupMapper;
import com.onlinestore.product_service.mapper.ProductOptionMapper;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductOptionGroup;
import com.onlinestore.product_service.repository.ProductOptionGroupRepository;
import com.onlinestore.product_service.repository.ProductOptionRepository;
import com.onlinestore.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOptionGroupService {
    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionGroupMapper productOptionGroupMapper;

    public List<ProductOptionGroupDto> getActiveOptionGroups(Long productId) {
        findActiveProductOrThrow(productId);
        return productOptionGroupRepository.findAllByProductIdAndActiveTrue(productId)
                .stream()
                .map(productOptionGroupMapper::toDto)
                .toList();
    }

    @Transactional
    public ProductOptionGroupDto createProductOptionGroup(Long productId, CreateProductOptionGroupCommand command) {
        Product product = findActiveProductOrThrow(productId);
        if (productOptionGroupRepository.existsByProductIdAndCode(productId, command.code())) {
            throw new ProductOptionGroupAlreadyExistsException(productId, command.code());
        }
        ProductOptionGroup group = productOptionGroupMapper.from(command);
        group.setProduct(product);
        group.setActive(true);
        group.validateConfiguration();
        ProductOptionGroup savedGroup = productOptionGroupRepository.save(group);
        return productOptionGroupMapper.toDto(savedGroup);
    }

    @Transactional
    public ProductOptionGroupDto updateOptionGroup(Long groupId, UpdateProductOptionGroupCommand command) {
        ProductOptionGroup group = productOptionGroupRepository.findByIdAndActiveTrue(groupId)
                .orElseThrow(() -> new ProductOptionGroupNotFoundException(groupId));
        Long productId = group.getProduct().getId();
        findActiveProductOrThrow(productId);
        if (productOptionGroupRepository.existsByProductIdAndCodeAndIdNot(productId, command.code(), groupId)) {
            throw new ProductOptionGroupAlreadyExistsException(productId, command.code());
        }
        group.update(command.name(), command.code(), command.selectionType(), command.required(),
                command.minSelections(), command.maxSelections());
        return productOptionGroupMapper.toDto(group);
    }

    private Product findActiveProductOrThrow(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
