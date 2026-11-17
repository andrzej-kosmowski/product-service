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
import com.onlinestore.product_service.model.*;
import com.onlinestore.product_service.repository.ProductOptionGroupRepository;
import com.onlinestore.product_service.repository.ProductOptionRepository;
import com.onlinestore.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {
    ProductService productService;
    ProductRepository productRepository;
    ProductOptionGroupRepository productOptionGroupRepository;
    ProductOptionRepository productOptionRepository;
    ProductMapper productMapper;
    ProductOptionGroupMapper productOptionGroupMapper;
    ProductOptionMapper productOptionMapper;

    @BeforeEach
    void setUp() {
        this.productRepository = Mockito.mock(ProductRepository.class);
        this.productOptionGroupRepository = Mockito.mock(ProductOptionGroupRepository.class);
        this.productOptionRepository = Mockito.mock(ProductOptionRepository.class);
        this.productMapper = Mappers.getMapper(ProductMapper.class);
        this.productOptionGroupMapper = Mappers.getMapper(ProductOptionGroupMapper.class);
        this.productOptionMapper = Mappers.getMapper(ProductOptionMapper.class);
        this.productService = new ProductService(
                productRepository, productOptionGroupRepository, productOptionRepository, productMapper,
                productOptionGroupMapper, productOptionMapper);
    }

    @Test
    void getActiveProducts_ProductsExist_ProductsReturned() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Product product1 = new Product();
        product1.setId(1L);
        product1.setSku("PC-001");
        product1.setName("Laptop");
        product1.setDescription("Gaming laptop");
        product1.setType(ProductType.COMPUTER);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setSku("PC-002");
        product2.setName("Smartphone");
        product2.setDescription("Smartphone description");
        product2.setType(ProductType.SMARTPHONE);
        ProductDto dto1 = new ProductDto(1L, "PC-001", "Laptop", "Gaming laptop",
                null, null, null, ProductType.COMPUTER
        );
        ProductDto dto2 = new ProductDto(2L, "PC-002", "Smartphone", "Smartphone description",
                null, null, null, ProductType.SMARTPHONE
        );
        Page<Product> products = new PageImpl<>(List.of(product1, product2));
        when(productRepository.findAllByActiveTrue(pageable))
                .thenReturn(products);
        // when
        Page<ProductDto> result = productService.getActiveProducts(pageable);
        // then
        assertAll(
            () -> assertEquals(2, result.getTotalElements()),
            () -> assertEquals(dto1, result.getContent().get(0)),
            () -> assertEquals(dto2, result.getContent().get(1))
        );
    }

    @Test
    void getActiveProductsByType_TypeProvided_ProductsReturned() {
        // given
        ProductType type = ProductType.COMPUTER;
        Pageable pageable = PageRequest.of(0, 10);
        Product product1 = createProduct(1L, "Laptop", "Gaming laptop", type);
        Product product2 = createProduct(2L, "Desktop", "Gaming desktop", type);
        Page<Product> products = new PageImpl<>(List.of(product1, product2));
        when(productRepository.findAllByTypeAndActiveTrue(type, pageable)).thenReturn(products);
        // when
        Page<ProductDto> result = productService.getActiveProductsByType(type, pageable);
        // then
        assertAll(
            () -> assertEquals(2, result.getTotalElements()),
            () -> assertEquals(1L, result.getContent().get(0).id()),
            () -> assertEquals("Laptop", result.getContent().get(0).name()),
            () -> assertEquals(2L, result.getContent().get(1).id()),
            () -> assertEquals("Desktop", result.getContent().get(1).name())
        );
        verify(productRepository).findAllByTypeAndActiveTrue(type, pageable);
    }

    @Test
    void getActiveProductById_ProductExist_ProductReturned() {
        // given
        Long productId = 1L;
        Product product = createProduct(productId, "Laptop", "Gaming laptop", ProductType.COMPUTER);
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(product));
        // when
        ProductDto result = productService.getActiveProductById(productId);
        // then
        assertAll(
            () -> assertEquals(productId, result.id()),
            () -> assertEquals("Laptop", result.name()),
            () -> assertEquals("Gaming laptop", result.description()),
            () -> assertEquals(ProductType.COMPUTER, result.type())
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
    }

    @Test
    void getActiveProductById_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductNotFoundException.class,
                () -> productService.getActiveProductById(productId)
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
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
        List<ProductOptionGroupDto> result = productService.getActiveOptionGroups(productId);
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
                () -> productService.getActiveOptionGroups(productId)
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
        verifyNoInteractions(productOptionGroupRepository);
    }

    @Test
    void getActiveOptions_GroupAndProductExist_ActiveOptionsReturned() {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        Product product = createProduct(productId, "Laptop", "Gaming laptop", ProductType.COMPUTER);
        ProductOptionGroup group = createOptionGroup(
                groupId, product, "Memory", "MEMORY", SelectionType.SINGLE);
        ProductOption option1 = createOption(100L, group, "16 GB", "16GB", "16 GB");
        ProductOption option2 = createOption(200L, group, "32 GB", "32GB", "32 GB");
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId)).thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(product));
        when(productOptionRepository.findAllByGroupIdAndActiveTrue(groupId)).thenReturn(List.of(option1, option2));
        // when
        List<ProductOptionDto> result = productService.getActiveOptions(groupId);
        // then
        assertAll(
            () -> assertEquals(2, result.size()),
            () -> assertEquals(100L, result.get(0).id()),
            () -> assertEquals("16 GB", result.get(0).name()),
            () -> assertEquals("16GB", result.get(0).code()),
            () -> assertEquals(200L, result.get(1).id()),
            () -> assertEquals("32 GB", result.get(1).name()),
            () -> assertEquals("32GB", result.get(1).code())
        );
        verify(productOptionGroupRepository).findByIdAndActiveTrue(groupId);
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionRepository).findAllByGroupIdAndActiveTrue(groupId);
    }

    @Test
    void getActiveOptions_GroupDoesNotExist_ProductOptionGroupNotFoundExceptionThrown() {
        // given
        Long groupId = 1L;
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductOptionGroupNotFoundException.class,
                () -> productService.getActiveOptions(groupId)
        );
        verify(productOptionGroupRepository).findByIdAndActiveTrue(groupId);
        verifyNoInteractions(productRepository, productOptionRepository);
    }

    @Test
    void getActiveOptions_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        Product product = new Product();
        product.setId(productId);
        ProductOptionGroup group = new ProductOptionGroup();
        group.setProduct(product);
        when(productOptionGroupRepository.findByIdAndActiveTrue(groupId)).thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductNotFoundException.class,
                () -> productService.getActiveOptions(groupId)
        );
        verify(productOptionGroupRepository).findByIdAndActiveTrue(groupId);
        verify(productRepository).findByIdAndActiveTrue(productId);
        verifyNoInteractions(productOptionRepository);
    }

    @Test
    void getProductConfiguration_ProductHasGroupsAndOptions_ConfigurationReturned() {
        // given
        Long productId = 1L;
        Long groupId = 10L;
        Product product = createProduct(productId, "Laptop", "Gaming laptop", ProductType.COMPUTER);
        ProductOptionGroup group = createOptionGroup(
                groupId, product, "Memory", "MEMORY", SelectionType.SINGLE);
        ProductOption option = createOption(100L, group, "16 GB", "16GB", "16 GB");
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productOptionGroupRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(List.of(group));
        when(productOptionRepository.findAllByGroupIdAndActiveTrue(groupId))
                .thenReturn(List.of(option));
        // when
        ProductConfigurationDto result =
                productService.getProductConfiguration(productId);
        // then
        assertAll(
                () -> assertEquals(productId, result.productId()),
                () -> assertEquals(1, result.groups().size()),
                () -> assertEquals(groupId, result.groups().getFirst().id()),
                () -> assertEquals("Memory", result.groups().getFirst().name()),
                () -> assertEquals(1, result.groups().getFirst().options().size()),
                () -> assertEquals(
                        "16 GB",
                        result.groups().getFirst().options().getFirst().value()
                )
        );

        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionGroupRepository)
                .findAllByProductIdAndActiveTrue(productId);
        verify(productOptionRepository)
                .findAllByGroupIdAndActiveTrue(groupId);
    }

    @Test
    void getProductConfiguration_ProductHasNoGroups_EmptyConfigurationReturned() {
        // given
        Long productId = 1L;
        Product product = createProduct(productId, "Computer", "Gaming computer", ProductType.COMPUTER);
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productOptionGroupRepository.findAllByProductIdAndActiveTrue(productId))
                .thenReturn(List.of());
        // when
        ProductConfigurationDto result = productService.getProductConfiguration(productId);
        // then
        assertEquals(productId, result.productId());
        assertTrue(result.groups().isEmpty());
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productOptionGroupRepository).findAllByProductIdAndActiveTrue(productId);
        verifyNoInteractions(productOptionRepository);
    }

    @Test
    void getProductConfiguration_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.empty());
        // when & then
        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductConfiguration(productId)
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
        verifyNoInteractions(productOptionGroupRepository, productOptionRepository);
    }

    @Test
    void createProduct_ValidCommand_ProductCreated() {
        // given
        CreateProductCommand command = new CreateProductCommand(
                "PC-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );

        Product savedProduct = Product.create(
                command.sku(),
                command.name(),
                command.description(),
                command.netPrice(),
                command.taxRate(),
                command.type()
        );
        savedProduct.setId(1L);
        when(productRepository.existsBySku(command.sku())).thenReturn(false);
        when(productRepository.save(Mockito.any(Product.class))).thenReturn(savedProduct);
        // when
        ProductDto result = productService.createProduct(command);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1L, result.id()),
                () -> assertEquals(command.sku(), result.sku()),
                () -> assertEquals(command.name(), result.name()),
                () -> assertEquals(command.description(), result.description()),
                () -> assertEquals(command.netPrice(), result.netPrice()),
                () -> assertEquals(new BigDecimal("6150.00"), result.grossPrice()),
                () -> assertEquals(command.taxRate(), result.taxRate()),
                () -> assertEquals(command.type(), result.type())
        );
        verify(productRepository).existsBySku(command.sku());
        verify(productRepository).save(Mockito.any(Product.class));
    }

    @Test
    void createProduct_ProductAlreadyExists_ProductAlreadyExistsExceptionThrown() {
        // given
        CreateProductCommand command = new CreateProductCommand(
                "PC-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productRepository.existsBySku(command.sku())).thenReturn(true);
        // when & then
        assertThrows(
                ProductAlreadyExistsException.class,
                () -> productService.createProduct(command)
        );
        verify(productRepository).existsBySku(command.sku());
        verify(productRepository, never()).save(Mockito.any(Product.class));
    }

    @Test
    void updateProduct_ValidCommand_ProductUpdated() {
        // given
        Long productId = 1L;
        Product product = Product.create(
                "PC-001",
                "Old Gaming PC",
                "Old description",
                new BigDecimal("4000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        product.setId(productId);
        UpdateProductCommand command = new UpdateProductCommand(
                "PC-002",
                "New Gaming PC",
                "New description",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(command.sku(), productId))
                .thenReturn(false);
        // when
        ProductDto result = productService.updateProduct(productId, command);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(productId, result.id()),
                () -> assertEquals(command.sku(), result.sku()),
                () -> assertEquals(command.name(), result.name()),
                () -> assertEquals(command.description(), result.description()),
                () -> assertEquals(command.netPrice(), result.netPrice()),
                () -> assertEquals(new BigDecimal("6150.00"), result.grossPrice()),
                () -> assertEquals(command.taxRate(), result.taxRate()),
                () -> assertEquals(command.type(), result.type())
        );
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productRepository).existsBySkuAndIdNot(command.sku(), productId);
    }

    @Test
    void updateProduct_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        UpdateProductCommand command = new UpdateProductCommand(
                "PC-002",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(productId, command));
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productRepository, never())
                .existsBySkuAndIdNot(command.sku(), productId);
        verify(productRepository, never()).save(Mockito.any(Product.class));
    }

    @Test
    void updateProduct_SkuAlreadyExists_ProductAlreadyExistsExceptionThrown() {
        // given
        Long productId = 1L;
        Product product = Product.create(
                "PC-001",
                "Old Gaming PC",
                "Old description",
                new BigDecimal("4000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        product.setId(productId);
        UpdateProductCommand command = new UpdateProductCommand(
                "PC-002",
                "New Gaming PC",
                "New description",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(command.sku(), productId))
                .thenReturn(true);
        // when & then
        assertThrows(
                ProductAlreadyExistsException.class,
                () -> productService.updateProduct(productId, command)
        );
        // then
        assertEquals("PC-001", product.getSku());
        assertEquals("Old Gaming PC", product.getName());
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productRepository).existsBySkuAndIdNot(command.sku(), productId);
        verify(productRepository, never()).save(Mockito.any(Product.class));
    }

    @Test
    void activateProduct_ProductExists_ProductActivated() {
        // given
        Long productId = 1L;
        Product product = Product.create(
                "PC-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        product.setId(productId);
        product.deactivate();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        // when
        productService.activateProduct(productId);
        // then
        assertTrue(product.getActive());
        verify(productRepository).findById(productId);
    }

    @Test
    void deactivateProduct_ProductExists_ProductDeactivated() {
        // given
        Long productId = 1L;
        Product product = Product.create(
                "PC-001",
                "Gaming PC",
                "Gaming computer",
                new BigDecimal("5000.00"),
                new BigDecimal("23.00"),
                ProductType.COMPUTER
        );
        product.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        // when
        productService.deactivateProduct(productId);
        // then
        assertFalse(product.getActive());
        verify(productRepository).findById(productId);
    }

    @Test
    void activateProduct_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductNotFoundException.class,
                () -> productService.activateProduct(productId)
        );
        verify(productRepository).findById(productId);
    }

    @Test
    void deactivateProduct_ProductDoesNotExist_ProductNotFoundExceptionThrown() {
        // given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(ProductNotFoundException.class,
                () -> productService.deactivateProduct(productId)
        );
        verify(productRepository).findById(productId);
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

    private ProductOptionGroup createOptionGroup(Long id, Product product, String name, String code,
            SelectionType selectionType) {
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

    private ProductOption createOption(Long id, ProductOptionGroup group, String name, String code,
            String value) {
        ProductOption option = new ProductOption();
        option.setId(id);
        option.setGroup(group);
        option.setName(name);
        option.setCode(code);
        option.setValue(value);
        return option;
    }
}