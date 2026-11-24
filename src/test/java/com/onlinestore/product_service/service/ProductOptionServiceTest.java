
package com.onlinestore.product_service.service;

import com.onlinestore.product_service.command.CreateProductOptionCommand;
import com.onlinestore.product_service.command.UpdateProductOptionCommand;
import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.exception.*;
import com.onlinestore.product_service.mapper.ProductOptionMapper;
import com.onlinestore.product_service.model.Product;
import com.onlinestore.product_service.model.ProductOption;
import com.onlinestore.product_service.model.ProductOptionGroup;
import com.onlinestore.product_service.repository.ProductOptionGroupRepository;
import com.onlinestore.product_service.repository.ProductOptionRepository;
import com.onlinestore.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductOptionServiceTest {

    ProductRepository productRepository;
    ProductOptionGroupRepository productOptionGroupRepository;
    ProductOptionRepository productOptionRepository;
    ProductOptionMapper productOptionMapper;

    ProductOptionService productOptionService;

    Product product;
    ProductOptionGroup group;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productOptionGroupRepository = mock(ProductOptionGroupRepository.class);
        productOptionRepository = mock(ProductOptionRepository.class);

        productOptionMapper = Mappers.getMapper(ProductOptionMapper.class);

        productOptionService = new ProductOptionService(
                productRepository,
                productOptionGroupRepository,
                productOptionRepository,
                productOptionMapper
        );

        product = new Product();
        product.setId(1L);
        product.setActive(true);

        group = new ProductOptionGroup();
        group.setId(10L);
        group.setProduct(product);
        group.setActive(true);
    }

    @Nested
    @DisplayName("getActiveOptions")
    class GetActiveOptions {

        @Test
        @DisplayName("Should return active options for group")
        void getActiveOptions_ActiveGroupAndProduct_OptionsReturned() {
            // given
            ProductOption option = createTextOption();
            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(product));
            when(productOptionRepository.findAllByGroupIdAndActiveTrue(10L))
                    .thenReturn(List.of(option));
            // when
            List<ProductOptionDto> result = productOptionService.getActiveOptions(10L);
            // then
            assertEquals(1, result.size());
            assertAll(
                    () -> assertEquals(100L, result.getFirst().id()),
                    () -> assertEquals("RAM 16 GB", result.getFirst().name()),
                    () -> assertEquals("RAM_16", result.getFirst().code()),
                    () -> assertEquals("16 GB", result.getFirst().value()),
                    () -> assertNull(result.getFirst().optionProduct())
            );

            verify(productOptionGroupRepository).findByIdAndActiveTrue(10L);
            verify(productRepository).findByIdAndActiveTrue(1L);
            verify(productOptionRepository).findAllByGroupIdAndActiveTrue(10L);
        }

        @Test
        @DisplayName("Should throw exception when group does not exist or is inactive")
        void getActiveOptions_GroupNotFound_ExceptionThrown() {
            // given
            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.empty());

            // when
            ProductOptionGroupNotFoundException exception = assertThrows(
                    ProductOptionGroupNotFoundException.class,
                    () -> productOptionService.getActiveOptions(10L)
            );

            // then
            assertNotNull(exception);

            verifyNoInteractions(productRepository);
            verifyNoInteractions(productOptionRepository);
        }

        @Test
        @DisplayName("Should throw exception when parent product is inactive")
        void getActiveOptions_ParentProductNotFound_ExceptionThrown() {
            // given
            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.empty());

            // when
            ProductNotFoundException exception = assertThrows(
                    ProductNotFoundException.class,
                    () -> productOptionService.getActiveOptions(10L)
            );

            // then
            assertNotNull(exception);

            verifyNoInteractions(productOptionRepository);
        }
    }

    @Nested
    @DisplayName("createOption")
    class CreateOption {

        @Test
        @DisplayName("Should create text option")
        void createOption_ValidTextOption_OptionCreated() {
            // given
            CreateProductOptionCommand command = createTextOptionCommand();

            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(product));
            when(productOptionRepository.existsByGroupIdAndCode(10L, "RAM_16"))
                    .thenReturn(false);
            when(productOptionRepository.save(any(ProductOption.class)))
                    .thenAnswer(invocation -> {
                        ProductOption option = invocation.getArgument(0);
                        option.setId(100L);
                        return option;
                    });

            // when
            ProductOptionDto result = productOptionService.createOption(10L, command);

            // then
            assertAll(
                    () -> assertEquals(100L, result.id()),
                    () -> assertEquals("RAM 16 GB", result.name()),
                    () -> assertEquals("RAM_16", result.code()),
                    () -> assertEquals("16 GB", result.value()),
                    () -> assertNull(result.optionProduct())
            );

            verify(productOptionRepository).save(any(ProductOption.class));
        }

        @Test
        @DisplayName("Should create product-backed option")
        void createOption_ValidProductOption_OptionCreated() {
            // given
            Product optionProduct = new Product();
            optionProduct.setId(2L);
            optionProduct.setActive(true);
            optionProduct.setName("SSD 1 TB");
            optionProduct.setSku("SSD-1TB");

            CreateProductOptionCommand command = new CreateProductOptionCommand(
                    "SSD 1 TB",
                    "SSD_1TB",
                    null,
                    2L
            );

            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(product));
            when(productRepository.findByIdAndActiveTrue(2L))
                    .thenReturn(Optional.of(optionProduct));
            when(productOptionRepository.existsByGroupIdAndCode(10L, "SSD_1TB"))
                    .thenReturn(false);
            when(productOptionRepository.save(any(ProductOption.class)))
                    .thenAnswer(invocation -> {
                        ProductOption option = invocation.getArgument(0);
                        option.setId(101L);
                        return option;
                    });

            // when
            ProductOptionDto result = productOptionService.createOption(10L, command);

            // then
            assertAll(
                    () -> assertEquals(101L, result.id()),
                    () -> assertEquals("SSD 1 TB", result.name()),
                    () -> assertEquals("SSD_1TB", result.code()),
                    () -> assertNull(result.value()),
                    () -> assertNotNull(result.optionProduct()),
                    () -> assertEquals(2L, result.optionProduct().id()),
                    () -> assertEquals("SSD-1TB", result.optionProduct().sku())
            );

            verify(productRepository).findByIdAndActiveTrue(2L);
            verify(productOptionRepository).save(any(ProductOption.class));
        }

        @Test
        @DisplayName("Should throw exception when group does not exist")
        void createOption_GroupNotFound_ExceptionThrown() {
            // given
            CreateProductOptionCommand command = createTextOptionCommand();

            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.empty());

            // when
            assertThrows(
                    ProductOptionGroupNotFoundException.class,
                    () -> productOptionService.createOption(10L, command)
            );

            // then
            verifyNoInteractions(productRepository);
            verifyNoInteractions(productOptionRepository);
        }

        @Test
        @DisplayName("Should throw exception when parent product is inactive")
        void createOption_ParentProductNotFound_ExceptionThrown() {
            // given
            CreateProductOptionCommand command = createTextOptionCommand();

            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.empty());

            // when
            assertThrows(
                    ProductNotFoundException.class,
                    () -> productOptionService.createOption(10L, command)
            );

            // then
            verifyNoInteractions(productOptionRepository);
        }

        @Test
        @DisplayName("Should throw exception when option product is inactive")
        void createOption_OptionProductNotFound_ExceptionThrown() {
            // given
            CreateProductOptionCommand command = new CreateProductOptionCommand(
                    "SSD 1 TB",
                    "SSD_1TB",
                    null,
                    2L
            );

            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(product));
            when(productOptionRepository.existsByGroupIdAndCode(10L, "SSD_1TB"))
                    .thenReturn(false);
            when(productRepository.findByIdAndActiveTrue(2L))
                    .thenReturn(Optional.empty());

            // when
            assertThrows(
                    ProductNotFoundException.class,
                    () -> productOptionService.createOption(10L, command)
            );

            // then
            verify(productRepository).findByIdAndActiveTrue(2L);
            verify(productOptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when option code already exists in group")
        void createOption_CodeAlreadyExists_ExceptionThrown() {
            // given
            CreateProductOptionCommand command = createTextOptionCommand();

            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(product));
            when(productOptionRepository.existsByGroupIdAndCode(10L, "RAM_16"))
                    .thenReturn(true);

            // when
            assertThrows(
                    ProductOptionAlreadyExistsException.class,
                    () -> productOptionService.createOption(10L, command)
            );

            // then
            verify(productOptionRepository, never()).save(any());
            verify(productRepository, never()).findByIdAndActiveTrue(2L);
        }

        @Test
        @DisplayName("Should throw exception when option has neither value nor product")
        void createOption_InvalidOption_ExceptionThrown() {
            // given
            CreateProductOptionCommand command = new CreateProductOptionCommand(
                    "Invalid option",
                    "INVALID",
                    null,
                    null
            );
            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(product));
            when(productOptionRepository.existsByGroupIdAndCode(10L, "INVALID"))
                    .thenReturn(false);
            // when
            assertThrows(InvalidProductOptionException.class,
                    () -> productOptionService.createOption(10L, command));
            // then
            verify(productOptionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateOption")
    class UpdateOption {
        @Test
        @DisplayName("Should update text option")
        void updateOption_ValidTextOption_OptionUpdated() {
            // given
            ProductOption option = createTextOption();
            UpdateProductOptionCommand command = updateTextOptionCommand();
            mockActiveOption(option);
            when(productOptionRepository.existsByGroupIdAndCodeAndIdNot(10L, "RAM_32", 100L))
                    .thenReturn(false);
            // when
            ProductOptionDto result = productOptionService.updateOption(100L, command);
            // then
            assertAll(
                    () -> assertEquals("RAM 32 GB", result.name()),
                    () -> assertEquals("RAM_32", result.code()),
                    () -> assertEquals("32 GB", result.value()),
                    () -> assertNull(result.optionProduct()),
                    () -> assertEquals("RAM 32 GB", option.getName()),
                    () -> assertEquals("RAM_32", option.getCode()),
                    () -> assertEquals("32 GB", option.getValue())
            );
            verify(productOptionRepository).findByIdAndActiveTrue(100L);
            verify(productOptionRepository).existsByGroupIdAndCodeAndIdNot(10L, "RAM_32", 100L);
            verify(productOptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update product-backed option")
        void updateOption_ValidProductOption_OptionUpdated() {
            // given
            Product oldOptionProduct = createOptionProduct(2L, "SSD 512 GB", "SSD-512");
            Product newOptionProduct = createOptionProduct(3L, "SSD 1 TB", "SSD-1TB");
            ProductOption option = createProductOption(oldOptionProduct);
            UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                    "SSD 1 TB",
                    "SSD_1TB",
                    null,
                    3L
            );
            mockActiveOption(option);
            when(productOptionRepository.existsByGroupIdAndCodeAndIdNot(10L, "SSD_1TB", 100L))
                    .thenReturn(false);
            when(productRepository.findByIdAndActiveTrue(3L))
                    .thenReturn(Optional.of(newOptionProduct));
            // when
            ProductOptionDto result = productOptionService.updateOption(100L, command);
            // then
            assertAll(
                    () -> assertEquals("SSD 1 TB", result.name()),
                    () -> assertEquals("SSD_1TB", result.code()),
                    () -> assertNull(result.value()),
                    () -> assertNotNull(result.optionProduct()),
                    () -> assertEquals(3L, result.optionProduct().id()),
                    () -> assertEquals("SSD-1TB", result.optionProduct().sku()),
                    () -> assertSame(newOptionProduct, option.getOptionProduct())
            );
            verify(productRepository).findByIdAndActiveTrue(3L);
            verify(productOptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should change text option into product-backed option")
        void updateOption_TextOptionChangedToProductOption_OptionUpdated() {
            // given
            ProductOption option = createTextOption();
            Product optionProduct = createOptionProduct(2L, "SSD 1 TB", "SSD-1TB");
            UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                    "SSD 1 TB",
                    "SSD_1TB",
                    null,
                    2L
            );
            mockActiveOption(option);
            when(productOptionRepository.existsByGroupIdAndCodeAndIdNot(10L, "SSD_1TB", 100L))
                    .thenReturn(false);
            when(productRepository.findByIdAndActiveTrue(2L))
                    .thenReturn(Optional.of(optionProduct));
            // when
            ProductOptionDto result = productOptionService.updateOption(100L, command);
            // then
            assertAll(
                    () -> assertNull(option.getValue()),
                    () -> assertSame(optionProduct, option.getOptionProduct()),
                    () -> assertNull(result.value()),
                    () -> assertEquals(2L, result.optionProduct().id())
            );
        }

        @Test
        @DisplayName("Should change product-backed option into text option")
        void updateOption_ProductOptionChangedToTextOption_OptionUpdated() {
            // given
            Product oldOptionProduct = createOptionProduct(2L, "SSD 512 GB", "SSD-512");
            ProductOption option = createProductOption(oldOptionProduct);
            UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                    "SSD 1 TB",
                    "SSD_1TB",
                    "1 TB",
                    null
            );
            mockActiveOption(option);
            when(productOptionRepository.existsByGroupIdAndCodeAndIdNot(10L, "SSD_1TB", 100L))
                    .thenReturn(false);
            // when
            ProductOptionDto result = productOptionService.updateOption(100L, command);
            // then
            assertAll(
                    () -> assertEquals("1 TB", option.getValue()),
                    () -> assertNull(option.getOptionProduct()),
                    () -> assertEquals("1 TB", result.value()),
                    () -> assertNull(result.optionProduct())
            );
        }

        @Test
        @DisplayName("Should throw exception when option does not exist or is inactive")
        void updateOption_OptionNotFound_ExceptionThrown() {
            // given
            UpdateProductOptionCommand command = updateTextOptionCommand();
            when(productOptionRepository.findByIdAndActiveTrue(100L))
                    .thenReturn(Optional.empty());
            // when
            assertThrows(ProductOptionNotFoundException.class,
                    () -> productOptionService.updateOption(100L, command));
            // then
            verifyNoInteractions(productOptionGroupRepository);
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Should throw exception when group does not exist or is inactive")
        void updateOption_GroupNotFound_ExceptionThrown() {
            // given
            ProductOption option = createTextOption();
            UpdateProductOptionCommand command = updateTextOptionCommand();
            when(productOptionRepository.findByIdAndActiveTrue(100L))
                    .thenReturn(Optional.of(option));
            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.empty());
            // when
            assertThrows(ProductOptionGroupNotFoundException.class,
                    () -> productOptionService.updateOption(100L, command));
            // then
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Should throw exception when parent product is inactive")
        void updateOption_ParentProductNotFound_ExceptionThrown() {
            // given
            ProductOption option = createTextOption();
            UpdateProductOptionCommand command = updateTextOptionCommand();
            when(productOptionRepository.findByIdAndActiveTrue(100L))
                    .thenReturn(Optional.of(option));
            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.empty());
            // when
            assertThrows(ProductNotFoundException.class,
                    () -> productOptionService.updateOption(100L, command));
            // then
            verify(productOptionRepository, never()).existsByGroupIdAndCodeAndIdNot(any(), any(), any());
        }

        @Test
        @DisplayName("Should throw exception when referenced product is inactive")
        void updateOption_OptionProductNotFound_ExceptionThrown() {
            // given
            ProductOption option = createTextOption();
            UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                    "SSD 1 TB",
                    "SSD_1TB",
                    null,
                    2L
            );
            mockActiveOption(option);
            when(productOptionRepository.existsByGroupIdAndCodeAndIdNot(10L, "SSD_1TB", 100L))
                    .thenReturn(false);
            when(productRepository.findByIdAndActiveTrue(2L))
                    .thenReturn(Optional.empty());
            // when
            assertThrows(ProductNotFoundException.class,
                    () -> productOptionService.updateOption(100L, command));
            // then
            verify(productOptionRepository, never()).save(any());
            assertEquals("16 GB", option.getValue());
        }

        @Test
        @DisplayName("Should throw exception when option code already exists in group")
        void updateOption_CodeAlreadyExists_ExceptionThrown() {
            // given
            ProductOption option = createTextOption();
            UpdateProductOptionCommand command = updateTextOptionCommand();
            mockActiveOption(option);
            when(productOptionRepository.existsByGroupIdAndCodeAndIdNot(10L, "RAM_32", 100L))
                    .thenReturn(true);
            // when
            assertThrows(ProductOptionAlreadyExistsException.class,
                    () -> productOptionService.updateOption(100L, command));
            // then
            assertEquals("RAM 16 GB", option.getName());
            assertEquals("RAM_16", option.getCode());
            assertEquals("16 GB", option.getValue());
            verify(productRepository, never()).findByIdAndActiveTrue(2L);
        }

        @Test
        @DisplayName("Should throw exception when option has both value and product")
        void updateOption_InvalidOption_ExceptionThrown() {
            // given
            ProductOption option = createTextOption();
            UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                    "Invalid option",
                    "INVALID",
                    "Some value",
                    2L
            );
            Product optionProduct = createOptionProduct(2L, "SSD 1 TB", "SSD-1TB");
            mockActiveOption(option);
            when(productOptionRepository.existsByGroupIdAndCodeAndIdNot(10L, "INVALID", 100L))
                    .thenReturn(false);
            when(productRepository.findByIdAndActiveTrue(2L))
                    .thenReturn(Optional.of(optionProduct));
            // when
            assertThrows(InvalidProductOptionException.class,
                    () -> productOptionService.updateOption(100L, command));
            // then
            assertEquals("RAM 16 GB", option.getName());
            assertEquals("RAM_16", option.getCode());
            assertEquals("16 GB", option.getValue());
            assertNull(option.getOptionProduct());
        }
    }

    @Nested
    @DisplayName("deactivateOption")
    class DeactivateOption {

        @Test
        @DisplayName("Should deactivate option")
        void deactivateOption_OptionExists_OptionDeactivated() {
            // given
            ProductOption option = createTextOption();
            option.setActive(true);
            when(productOptionRepository.findById(100L))
                    .thenReturn(Optional.of(option));
            // when
            productOptionService.deactivateOption(100L);
            // then
            assertFalse(option.getActive());
            verify(productOptionRepository).findById(100L);
            verifyNoInteractions(productRepository, productOptionGroupRepository);
        }

        @Test
        @DisplayName("Should allow deactivating an already inactive option")
        void deactivateOption_OptionAlreadyInactive_NoExceptionThrown() {
            // given
            ProductOption option = createTextOption();
            option.setActive(false);
            when(productOptionRepository.findById(100L))
                    .thenReturn(Optional.of(option));
            // when
            productOptionService.deactivateOption(100L);
            // then
            assertFalse(option.getActive());
            verify(productOptionRepository).findById(100L);
            verifyNoInteractions(productRepository, productOptionGroupRepository);
        }

        @Test
        @DisplayName("Should throw exception when option does not exist")
        void deactivateOption_OptionNotFound_ExceptionThrown() {
            // given
            when(productOptionRepository.findById(100L))
                    .thenReturn(Optional.empty());
            // when & then
            assertThrows(ProductOptionNotFoundException.class,
                    () -> productOptionService.deactivateOption(100L));
            verify(productOptionRepository).findById(100L);
            verifyNoInteractions(productRepository, productOptionGroupRepository);
        }
    }

    @Nested
    @DisplayName("activateOption")
    class ActivateOption {

        @Test
        @DisplayName("Should activate option when group and product are active")
        void activateOption_OptionAndParentsActive_OptionActivated() {
            // given
            ProductOption option = createTextOption();
            option.setActive(false);
            when(productOptionRepository.findById(100L))
                    .thenReturn(Optional.of(option));
            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(product));
            // when
            productOptionService.activateOption(100L);
            // then
            assertTrue(option.getActive());
            verify(productOptionRepository).findById(100L);
            verify(productOptionGroupRepository).findByIdAndActiveTrue(10L);
            verify(productRepository).findByIdAndActiveTrue(1L);
        }

        @Test
        @DisplayName("Should throw exception when option does not exist")
        void activateOption_OptionNotFound_ExceptionThrown() {
            // given
            when(productOptionRepository.findById(100L))
                    .thenReturn(Optional.empty());
            // when & then
            assertThrows(ProductOptionNotFoundException.class,
                    () -> productOptionService.activateOption(100L));
            verifyNoInteractions(productRepository, productOptionGroupRepository);
        }

        @Test
        @DisplayName("Should throw exception when group is inactive or does not exist")
        void activateOption_GroupNotFound_ExceptionThrown() {
            // given
            ProductOption option = createTextOption();
            option.setActive(false);
            when(productOptionRepository.findById(100L))
                    .thenReturn(Optional.of(option));
            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.empty());
            // when & then
            assertThrows(ProductOptionGroupNotFoundException.class,
                    () -> productOptionService.activateOption(100L));
            assertFalse(option.getActive());
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Should throw exception when parent product is inactive or does not exist")
        void activateOption_ProductNotFound_ExceptionThrown() {
            // given
            ProductOption option = createTextOption();
            option.setActive(false);
            when(productOptionRepository.findById(100L))
                    .thenReturn(Optional.of(option));
            when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(group));
            when(productRepository.findByIdAndActiveTrue(1L))
                    .thenReturn(Optional.empty());
            // when & then
            assertThrows(ProductNotFoundException.class,
                    () -> productOptionService.activateOption(100L));
            assertFalse(option.getActive());
        }
    }

    private ProductOption createTextOption() {
        ProductOption option = new ProductOption();
        option.setId(100L);
        option.setName("RAM 16 GB");
        option.setCode("RAM_16");
        option.setValue("16 GB");
        option.setActive(true);
        option.setGroup(group);
        return option;
    }

    private CreateProductOptionCommand createTextOptionCommand() {
        return new CreateProductOptionCommand(
                "RAM 16 GB",
                "RAM_16",
                "16 GB",
                null
        );
    }

    private UpdateProductOptionCommand updateTextOptionCommand() {
        return new UpdateProductOptionCommand(
                "RAM 32 GB",
                "RAM_32",
                "32 GB",
                null
        );
    }

    private ProductOption createProductOption(Product optionProduct) {
        ProductOption option = new ProductOption();
        option.setId(100L);
        option.setName("SSD 512 GB");
        option.setCode("SSD_512");
        option.setOptionProduct(optionProduct);
        option.setActive(true);
        option.setGroup(group);
        return option;
    }

    private Product createOptionProduct(Long id, String name, String sku) {
        Product optionProduct = new Product();
        optionProduct.setId(id);
        optionProduct.setName(name);
        optionProduct.setSku(sku);
        optionProduct.setActive(true);
        return optionProduct;
    }

    private void mockActiveOption(ProductOption option) {
        when(productOptionRepository.findByIdAndActiveTrue(100L))
                .thenReturn(Optional.of(option));
        when(productOptionGroupRepository.findByIdAndActiveTrue(10L))
                .thenReturn(Optional.of(group));
        when(productRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(product));
    }
}
