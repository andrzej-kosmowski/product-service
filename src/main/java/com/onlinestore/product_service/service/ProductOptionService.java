package com.onlinestore.product_service.service;

import com.onlinestore.product_service.command.CreateProductOptionCommand;
import com.onlinestore.product_service.command.UpdateProductOptionCommand;
import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.exception.ProductOptionAlreadyExistsException;
import com.onlinestore.product_service.exception.ProductOptionGroupNotFoundException;
import com.onlinestore.product_service.exception.ProductOptionNotFoundException;
import com.onlinestore.product_service.mapper.ProductOptionMapper;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductOption;
import com.onlinestore.product_service.model.ProductOptionGroup;
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
public class ProductOptionService {
    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionMapper productOptionMapper;

    public List<ProductOptionDto> getActiveOptions(Long groupId) {
        log.info("Fetching active options for group with id {}", groupId);
        ProductOptionGroup group = findActiveGroupOrThrow(groupId);
        findActiveProductOrThrow(group.getProduct().getId());
        List<ProductOptionDto> options = productOptionRepository.findAllByGroupIdAndActiveTrue(groupId)
                .stream()
                .filter(this::isAvailableOption)
                .map(productOptionMapper::toDto)
                .toList();
        log.info("Fetched {} active options for group with id {}", options.size(), groupId);
        return options;
    }

    @Transactional
    public ProductOptionDto createOption(Long groupId, CreateProductOptionCommand command) {
        log.info("Creating option with code {} in group with id {}", command.code(), groupId);
        ProductOptionGroup group = findActiveGroupOrThrow(groupId);
        findActiveProductOrThrow(group.getProduct().getId());
        if (productOptionRepository.existsByGroupIdAndCode(groupId, command.code())) {
            throw new ProductOptionAlreadyExistsException(groupId, command.code());
        }
        Product optionProduct = command.optionProductId() != null
                ? findActiveProductOrThrow(command.optionProductId())
                : null;
        ProductOption option = productOptionMapper.from(command, group, optionProduct);
        option.initialize();
        ProductOption savedOption = productOptionRepository.save(option);
        log.info("Created option with id {} in group with id {}", savedOption.getId(), groupId);
        return productOptionMapper.toDto(savedOption);
    }

    @Transactional
    public ProductOptionDto updateOption(Long optionId, UpdateProductOptionCommand command) {
        log.info("Updating option with id {}", optionId);
        ProductOption option = productOptionRepository.findByIdAndActiveTrue(optionId)
                .orElseThrow(() -> new ProductOptionNotFoundException(optionId));
        Long groupId = option.getGroup().getId();
        ProductOptionGroup group = productOptionGroupRepository.findByIdAndActiveTrue(groupId)
                .orElseThrow(() -> new ProductOptionGroupNotFoundException(groupId));
        findActiveProductOrThrow(group.getProduct().getId());
        if (productOptionRepository.existsByGroupIdAndCodeAndIdNot(groupId, command.code(), optionId)) {
            throw new ProductOptionAlreadyExistsException(groupId, command.code());
        }
        Product optionProduct = command.optionProductId() != null
                ? findActiveProductOrThrow(command.optionProductId())
                : null;
        option.update(
                command.name(),
                command.code(),
                command.value(),
                optionProduct
        );
        log.info("Updated option with id {}", optionId);
        return productOptionMapper.toDto(option);
    }

    @Transactional
    public void deactivateOption(Long optionId) {
        log.info("Deactivating option with id {}", optionId);
        ProductOption option = findProductOptionOrThrow(optionId);
        option.deactivate();
        log.info("Deactivated option with id {}", optionId);
    }

    @Transactional
    public void activateOption(Long optionId) {
        log.info("Activating option with id {}", optionId);
        ProductOption option = findProductOptionOrThrow(optionId);
        ProductOptionGroup group = findActiveGroupOrThrow(option.getGroup().getId());
        findActiveProductOrThrow(group.getProduct().getId());
        option.activate();
        log.info("Activated option with id {}", optionId);
    }

    private Product findActiveProductOrThrow(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductOptionGroup findActiveGroupOrThrow(Long id) {
        return productOptionGroupRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductOptionGroupNotFoundException(id));
    }

    private ProductOption findProductOptionOrThrow(Long id) {
        return productOptionRepository.findById(id)
                .orElseThrow(() -> new ProductOptionNotFoundException(id));
    }

    private boolean isAvailableOption(ProductOption option) {
        Product optionProduct = option.getOptionProduct();
        return optionProduct == null || Boolean.TRUE.equals(optionProduct.getActive());
    }

}
