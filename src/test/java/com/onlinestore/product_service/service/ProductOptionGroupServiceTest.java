package com.onlinestore.product_service.service;

import com.onlinestore.product_service.command.CreateProductOptionGroupCommand;
import com.onlinestore.product_service.command.UpdateProductOptionGroupCommand;
import com.onlinestore.product_service.dto.ProductOptionGroupDto;
import com.onlinestore.product_service.exception.InvalidProductOptionGroupException;
import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.exception.ProductOptionGroupAlreadyExistsException;
import com.onlinestore.product_service.exception.ProductOptionGroupNotFoundException;
import com.onlinestore.product_service.mapper.ProductOptionGroupMapper;
import com.onlinestore.product_service.model.*;
import com.onlinestore.product_service.repository.ProductOptionGroupRepository;
import com.onlinestore.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ProductOptionGroupServiceTest {
    ProductRepository productRepository;
    ProductOptionGroupRepository productOptionGroupRepository;
    ProductOptionGroupMapper productOptionGroupMapper;

    ProductOptionGroupService productOptionGroupService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productOptionGroupRepository = mock(ProductOptionGroupRepository.class);
        productOptionGroupMapper = Mappers.getMapper(ProductOptionGroupMapper.class);

        productOptionGroupService = new ProductOptionGroupService(
                productRepository,
                productOptionGroupRepository,
                productOptionGroupMapper
        );
    }

    @Test
    void getActiveOptionGroups_ProductExists_ActiveGroupsReturned() {
        // given
        Long productId = 1L;
        Product product = createProduct(productId, "Laptop", "Gaming laptop", ProductType.COMPUTER);
        ProductOptionGroup group1 = createOptionGroup(
                10L, product, "Memory", "MEMORY", SelectionType.SINGLE);
        ProductOptionGroup group2 = createOptionGroup(
                20L, product, "Storage", "STORAGE", SelectionType.MULTIPLE);

        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(product));
        when(productOptionGroupRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(List.of(group1, group2));
        // when
        List<ProductOptionGroupDto> result = productOptionGroupService.getActiveOptionGroups(productId);
        // then
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(10L, result.get(0).id()),
                () -> assertEquals("Memory", result.get(0).name()),
                () -> assertEquals("MEMORY", result.get(0).code()),
                () -> assertEquals(20L, result.get(1).id()),
                () -> assertEquals("Storage", result.get(1).name()),
                () -> assertEquals("STORAGE", result.get(1).code())
        );

        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionGroupRepository).findAllByProductIdAndActiveTrue(productId);
    }

    @Test
    void getActiveOptionGroups_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductNotFoundException.class,
                () -> productOptionGroupService.getActiveOptionGroups(productId)
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
        verifyNoInteractions(productOptionGroupRepository);
    }

    @Test
    void createOptionGroup_ProductExistsAndCodeIsUnique_GroupCreated() {
        // given
        Long productId = 1L;
        Product product = createProduct(
                productId, "Laptop", "Gaming laptop", ProductType.COMPUTER);
        CreateProductOptionGroupCommand command = new CreateProductOptionGroupCommand(
                "Memory",
                "MEMORY",
                SelectionType.SINGLE,
                true,
                1,
                1
        );
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productOptionGroupRepository.existsByProductIdAndCode(productId, "MEMORY"))
                .thenReturn(false);
        when(productOptionGroupRepository.save(any(ProductOptionGroup.class)))
                .thenAnswer(invocation -> {
                    ProductOptionGroup group = invocation.getArgument(0);
                    group.setId(10L);
                    return group;
                });
        // when
        ProductOptionGroupDto result =
                productOptionGroupService.createProductOptionGroup(productId, command);
        // then
        ArgumentCaptor<ProductOptionGroup> captor =
                ArgumentCaptor.forClass(ProductOptionGroup.class);

        verify(productOptionGroupRepository).save(captor.capture());
        ProductOptionGroup savedGroup = captor.getValue();
        assertAll(
                () -> assertEquals(10L, result.id()),
                () -> assertEquals("Memory", result.name()),
                () -> assertEquals("MEMORY", result.code()),
                () -> assertEquals(SelectionType.SINGLE, result.selectionType()),
                () -> assertEquals(true, result.required()),
                () -> assertEquals(1, result.minSelections()),
                () -> assertEquals(1, result.maxSelections()),
                () -> assertEquals(List.of(), result.options()),
                () -> assertEquals(product, savedGroup.getProduct()),
                () -> assertTrue(savedGroup.getActive()),
                () -> assertEquals("Memory", savedGroup.getName()),
                () -> assertEquals("MEMORY", savedGroup.getCode()),
                () -> assertEquals(SelectionType.SINGLE, savedGroup.getSelectionType())
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionGroupRepository).existsByProductIdAndCode(productId, "MEMORY");
    }

    @Test
    void createOptionGroup_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        CreateProductOptionGroupCommand command = new CreateProductOptionGroupCommand(
                "Memory", "MEMORY", SelectionType.SINGLE, true, 1, 1
        );
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.empty());
        // when & then
        assertThrows(
                ProductNotFoundException.class,
                () -> productOptionGroupService.createProductOptionGroup(productId, command)
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
        verifyNoInteractions(productOptionGroupRepository);
    }

    @Test
    void createOptionGroup_CodeAlreadyExists_ProductOptionGroupAlreadyExistsExceptionThrown() {
        // given
        Long productId = 1L;
        Product product = createProduct(
                productId, "Laptop", "Gaming laptop", ProductType.COMPUTER);
        CreateProductOptionGroupCommand command = new CreateProductOptionGroupCommand(
                "Memory", "MEMORY", SelectionType.SINGLE, true, 1, 1);
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productOptionGroupRepository.existsByProductIdAndCode(productId, "MEMORY"))
                .thenReturn(true);
        // when & then
        assertThrows(
                ProductOptionGroupAlreadyExistsException.class,
                () -> productOptionGroupService.createProductOptionGroup(productId, command)
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionGroupRepository)
                .existsByProductIdAndCode(productId, "MEMORY");
        verify(productOptionGroupRepository, never()).save(any());
    }


    @Test
    void createOptionGroup_InvalidConfiguration_IllegalArgumentExceptionThrown() {
        // given
        Long productId = 1L;
        Product product = createProduct(
                productId, "Laptop", "Gaming laptop", ProductType.COMPUTER);
        CreateProductOptionGroupCommand command = new CreateProductOptionGroupCommand(
                "Memory",
                "MEMORY",
                SelectionType.SINGLE,
                true,
                0,
                1
        );
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productOptionGroupRepository.existsByProductIdAndCode(productId, "MEMORY"))
                .thenReturn(false);
        // when & then
        assertThrows(
                InvalidProductOptionGroupException.class,
                () -> productOptionGroupService.createProductOptionGroup(productId, command)
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionGroupRepository).existsByProductIdAndCode(productId, "MEMORY");
        verify(productOptionGroupRepository, never()).save(any());
    }

    @Test
    void updateOptionGroup_GroupExistsAndCodeIsUnique_GroupUpdated() {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        Product product = new Product();
        product.setId(productId);
        product.setActive(true);
        ProductOptionGroup group = createOptionGroup(
                groupId,
                product,
                "Memory",
                "MEMORY",
                SelectionType.SINGLE
        );
        group.setActive(true);
        UpdateProductOptionGroupCommand command = new UpdateProductOptionGroupCommand(
                "Storage",
                "STORAGE",
                SelectionType.MULTIPLE,
                true,
                1,
                3
        );
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId))
                .thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productOptionGroupRepository.existsByProductIdAndCodeAndIdNot(
                productId, command.code(), groupId))
                .thenReturn(false);
        // when
        ProductOptionGroupDto result = productOptionGroupService.updateOptionGroup(groupId, command);
        // then
        assertAll(
                () -> assertEquals(groupId, result.id()),
                () -> assertEquals("Storage", result.name()),
                () -> assertEquals("STORAGE", result.code()),
                () -> assertEquals(SelectionType.MULTIPLE, result.selectionType()),
                () -> assertEquals(true, result.required()),
                () -> assertEquals(1, result.minSelections()),
                () -> assertEquals(3, result.maxSelections())
        );
        assertEquals("Storage", group.getName());
        assertEquals("STORAGE", group.getCode());
        verify(productOptionGroupRepository).findByIdAndActiveTrue(groupId);
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionGroupRepository).existsByProductIdAndCodeAndIdNot(
                productId, command.code(), groupId
        );
        verify(productOptionGroupRepository, never()).save(any());
    }

    @Test
    void updateOptionGroup_GroupDoesNotExist_ProductOptionGroupNotFoundExceptionThrown() {
        // given
        Long groupId = 1L;
        UpdateProductOptionGroupCommand command = new UpdateProductOptionGroupCommand(
                "Storage",
                "STORAGE",
                SelectionType.MULTIPLE,
                true,
                1,
                3
        );
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                ProductOptionGroupNotFoundException.class,
                () -> productOptionGroupService.updateOptionGroup(groupId, command)
        );
        verify(productOptionGroupRepository).findByIdAndActiveTrue(groupId);
        verifyNoInteractions(productRepository);
        verify(productOptionGroupRepository, never())
                .existsByProductIdAndCodeAndIdNot(any(), any(), any());
    }

    @Test
    void updateOptionGroup_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        Product product = new Product();
        product.setId(productId);
        ProductOptionGroup group = createOptionGroup(
                groupId,
                product,
                "Memory",
                "MEMORY",
                SelectionType.SINGLE
        );
        group.setActive(true);
        UpdateProductOptionGroupCommand command = new UpdateProductOptionGroupCommand(
                "Storage",
                "STORAGE",
                SelectionType.MULTIPLE,
                true,
                1,
                3
        );
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId))
                .thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.empty());
        // when & then
        assertThrows(
                ProductNotFoundException.class,
                () -> productOptionGroupService.updateOptionGroup(groupId, command)
        );
        verify(productOptionGroupRepository).findByIdAndActiveTrue(groupId);
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionGroupRepository, never())
                .existsByProductIdAndCodeAndIdNot(any(), any(), any());
    }

    @Test
    void updateOptionGroup_CodeAlreadyExists_ProductOptionGroupAlreadyExistsExceptionThrown() {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        Product product = new Product();
        product.setId(productId);
        product.setActive(true);
        ProductOptionGroup group = createOptionGroup(
                groupId,
                product,
                "Memory",
                "MEMORY",
                SelectionType.SINGLE
        );
        group.setActive(true);
        UpdateProductOptionGroupCommand command = new UpdateProductOptionGroupCommand(
                "Storage",
                "STORAGE",
                SelectionType.MULTIPLE,
                true,
                1,
                3
        );
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId))
                .thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productOptionGroupRepository.existsByProductIdAndCodeAndIdNot(
                productId, command.code(), groupId
        )).thenReturn(true);
        // when & then
        assertThrows(
                ProductOptionGroupAlreadyExistsException.class,
                () -> productOptionGroupService.updateOptionGroup(groupId, command)
        );
        assertEquals("Memory", group.getName());
        assertEquals("MEMORY", group.getCode());

        verify(productOptionGroupRepository).existsByProductIdAndCodeAndIdNot(
                productId, command.code(), groupId
        );
        verify(productOptionGroupRepository, never()).save(any());
    }

    @Test
    void updateOptionGroup_InvalidConfiguration_IllegalArgumentExceptionThrown() {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        Product product = new Product();
        product.setId(productId);
        product.setActive(true);
        ProductOptionGroup group = createOptionGroup(
                groupId,
                product,
                "Memory",
                "MEMORY",
                SelectionType.SINGLE
        );
        group.setActive(true);
        UpdateProductOptionGroupCommand command = new UpdateProductOptionGroupCommand(
                "Storage",
                "STORAGE",
                SelectionType.SINGLE,
                true,
                1,
                3
        );
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId))
                .thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productOptionGroupRepository.existsByProductIdAndCodeAndIdNot(
                productId, command.code(), groupId
        )).thenReturn(false);
        // when & then
        assertThrows(
                InvalidProductOptionGroupException.class,
                () -> productOptionGroupService.updateOptionGroup(groupId, command)
        );
        assertEquals("Storage", group.getName());
        assertEquals("STORAGE", group.getCode());
    }

    @Test
    void updateOptionGroup_SameCode_GroupUpdated() {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        Product product = new Product();
        product.setId(productId);
        product.setActive(true);
        ProductOptionGroup group = createOptionGroup(
                groupId,
                product,
                "Memory",
                "MEMORY",
                SelectionType.SINGLE
        );
        group.setActive(true);
        UpdateProductOptionGroupCommand command = new UpdateProductOptionGroupCommand(
                "Updated memory",
                "MEMORY",
                SelectionType.SINGLE,
                false,
                0,
                1
        );
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId))
                .thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productOptionGroupRepository.existsByProductIdAndCodeAndIdNot(
                productId, command.code(), groupId
        )).thenReturn(false);
        // when
        ProductOptionGroupDto result = productOptionGroupService.updateOptionGroup(groupId, command);
        // then
        assertAll(
                () -> assertEquals("Updated memory", result.name()),
                () -> assertEquals("MEMORY", result.code()),
                () -> assertEquals(false, result.required()),
                () -> assertEquals(0, result.minSelections()),
                () -> assertEquals(1, result.maxSelections())
        );
        verify(productOptionGroupRepository).existsByProductIdAndCodeAndIdNot(productId, "MEMORY", groupId);
    }

    @Test
    void validateConfiguration_SingleWithMinGreaterThanOne_ThrowsException() {
        ProductOptionGroup group = createOptionGroup(
                1L, null, "Memory", "MEMORY", SelectionType.SINGLE
        );
        group.setRequired(true);
        group.setMinSelections(2);
        group.setMaxSelections(null);
        assertThrows(InvalidProductOptionGroupException.class,
                group::validateConfiguration);
    }

    @Test
    void validateConfiguration_SingleWithMaxGreaterThanOne_ThrowsException() {
        ProductOptionGroup group = createOptionGroup(
                1L, null, "Memory", "MEMORY", SelectionType.SINGLE);
        group.setRequired(true);
        group.setMinSelections(1);
        group.setMaxSelections(2);
        assertThrows(InvalidProductOptionGroupException.class,
                group::validateConfiguration);
    }

    @Test
    void validateConfiguration_SingleWithOneSelection_IsValid() {
        ProductOptionGroup group = createOptionGroup(
                1L, null, "Memory", "MEMORY", SelectionType.SINGLE);
        group.setRequired(true);
        group.setMinSelections(1);
        group.setMaxSelections(1);
        assertDoesNotThrow(group::validateConfiguration);
    }

    private Product createProduct(Long id, String name, String description, ProductType type) {
        Product product = new Product();
        product.setId(id);
        product.setSku("SKU-" + id);
        product.setName(name);
        product.setDescription(description);
        product.setType(type);
        return product;
    }

    private ProductOptionGroup createOptionGroup(
            Long id,
            Product product,
            String name,
            String code,
            SelectionType selectionType
    ) {
        ProductOptionGroup group = new ProductOptionGroup();
        group.setId(id);
        group.setProduct(product);
        group.setName(name);
        group.setCode(code);
        group.setSelectionType(selectionType);
        group.setRequired(false);
        group.setMinSelections(0);
        return group;
    }

    private ProductOption createOption(
            Long id,
            ProductOptionGroup group,
            String name,
            String code,
            String value
    ) {
        ProductOption option = new ProductOption();
        option.setId(id);
        option.setGroup(group);
        option.setName(name);
        option.setCode(code);
        option.setValue(value);
        return option;
    }
}
