
package com.onlinestore.product_service.controller;

import com.onlinestore.product_service.command.CreateProductOptionGroupCommand;
import com.onlinestore.product_service.command.UpdateProductOptionGroupCommand;
import com.onlinestore.product_service.dto.ProductOptionGroupDto;
import com.onlinestore.product_service.exception.ProductNotFoundException;
import com.onlinestore.product_service.exception.ProductOptionGroupAlreadyExistsException;
import com.onlinestore.product_service.exception.ProductOptionGroupNotFoundException;
import com.onlinestore.product_service.exception.ProductServiceExceptionHandler;
import com.onlinestore.product_service.model.SelectionType;
import com.onlinestore.product_service.service.ProductOptionGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductOptionGroupController.class)
@Import(ProductServiceExceptionHandler.class)
class ProductOptionGroupControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductOptionGroupService productOptionGroupService;

    @Test
    void getActiveOptionGroups_ProductExistsAndHasGroups_ReturnsGroups() throws Exception {
        // given
        Long productId = 1L;
        List<ProductOptionGroupDto> groups = List.of(
                new ProductOptionGroupDto(
                        10L,
                        "Processor",
                        "PROCESSOR",
                        SelectionType.SINGLE,
                        true,
                        1,
                        1,
                        List.of()
                )
        );
        when(productOptionGroupService.getActiveOptionGroups(productId))
                .thenReturn(groups);
        // when & then
        mockMvc.perform(get("/api/option-groups")
                        .param("productId", productId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Processor"))
                .andExpect(jsonPath("$[0].code").value("PROCESSOR"));
        verify(productOptionGroupService).getActiveOptionGroups(productId);
    }

    @Test
    void getActiveOptionGroups_ProductExistsAndHasNoGroups_ReturnsEmptyList() throws Exception {
        // given
        Long productId = 1L;
        when(productOptionGroupService.getActiveOptionGroups(productId))
                .thenReturn(List.of());
        // when & then
        mockMvc.perform(get("/api/option-groups")
                        .param("productId", productId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        verify(productOptionGroupService).getActiveOptionGroups(productId);
    }

    @Test
    void getActiveOptionGroups_ProductDoesNotExist_ThrowsProductNotFoundException() throws Exception {
        // given
        Long productId = 1L;
        when(productOptionGroupService.getActiveOptionGroups(productId))
                .thenThrow(new ProductNotFoundException(productId));
        // when & then
        mockMvc.perform(get("/api/option-groups")
                        .param("productId", productId.toString()))
                .andExpect(status().isNotFound());
        verify(productOptionGroupService).getActiveOptionGroups(productId);
    }

    @Test
    void getActiveOptionGroups_ProductIdMissing_BadRequestReturned() throws Exception {
        mockMvc.perform(get("/api/option-groups"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter: productId"));

        verifyNoInteractions(productOptionGroupService);
    }

    @Test
    void updateOptionGroup_ValidCommand_GroupUpdated() throws Exception {
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
        ProductOptionGroupDto result = new ProductOptionGroupDto(
                groupId,
                "Storage",
                "STORAGE",
                SelectionType.MULTIPLE,
                true,
                1,
                3,
                List.of()
        );
        when(productOptionGroupService.updateOptionGroup(groupId, command))
                .thenReturn(result);
        // when & then
        mockMvc.perform(put("/api/option-groups/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Storage"))
                .andExpect(jsonPath("$.code").value("STORAGE"))
                .andExpect(jsonPath("$.selectionType").value("MULTIPLE"))
                .andExpect(jsonPath("$.required").value(true))
                .andExpect(jsonPath("$.minSelections").value(1))
                .andExpect(jsonPath("$.maxSelections").value(3));
        verify(productOptionGroupService).updateOptionGroup(groupId, command);
    }

    @Test
    void updateOptionGroup_InvalidCommand_BadRequestReturned() throws Exception {
        // given
        Long groupId = 1L;
        UpdateProductOptionGroupCommand command = new UpdateProductOptionGroupCommand(
                "",
                "STORAGE",
                SelectionType.MULTIPLE,
                true,
                1,
                3
        );
        // when & then
        mockMvc.perform(put("/api/option-groups/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productOptionGroupService);
    }

    @Test
    void updateOptionGroup_GroupDoesNotExist_NotFoundReturned() throws Exception {
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
        when(productOptionGroupService.updateOptionGroup(groupId, command))
                .thenThrow(new ProductOptionGroupNotFoundException(groupId));
        // when & then
        mockMvc.perform(put("/api/option-groups/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound());
        verify(productOptionGroupService).updateOptionGroup(groupId, command);
    }

    @Test
    void updateOptionGroup_ProductDoesNotExist_NotFoundReturned() throws Exception {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        UpdateProductOptionGroupCommand command = new UpdateProductOptionGroupCommand(
                "Storage",
                "STORAGE",
                SelectionType.MULTIPLE,
                true,
                1,
                3
        );
        when(productOptionGroupService.updateOptionGroup(groupId, command))
                .thenThrow(new ProductNotFoundException(productId));

        // when & then
        mockMvc.perform(put("/api/option-groups/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound());
        verify(productOptionGroupService).updateOptionGroup(groupId, command);
    }

    @Test
    void updateOptionGroup_CodeAlreadyExists_ConflictReturned() throws Exception {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        UpdateProductOptionGroupCommand command = new UpdateProductOptionGroupCommand(
                "Storage",
                "STORAGE",
                SelectionType.MULTIPLE,
                true,
                1,
                3
        );
        when(productOptionGroupService.updateOptionGroup(groupId, command))
                .thenThrow(new ProductOptionGroupAlreadyExistsException(productId, command.code()));
        // when & then
        mockMvc.perform(put("/api/option-groups/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isConflict());
        verify(productOptionGroupService).updateOptionGroup(groupId, command);
    }

    @Test
    void createOptionGroup_ProductIdMissing_BadRequestReturned() throws Exception {
        CreateProductOptionGroupCommand command = new CreateProductOptionGroupCommand(
                "Storage",
                "STORAGE",
                SelectionType.MULTIPLE,
                true,
                1,
                3
        );
        mockMvc.perform(post("/api/option-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter: productId"));

        verifyNoInteractions(productOptionGroupService);
    }

    @Test
    void deactivateOptionGroup_GroupExists_NoContentReturned() throws Exception {
        Long groupId = 10L;

        mockMvc.perform(patch("/api/option-groups/{groupId}/deactivate", groupId))
                .andExpect(status().isNoContent());

        verify(productOptionGroupService).deactivateOptionGroup(groupId);
    }

    @Test
    void deactivateOptionGroup_GroupDoesNotExist_NotFoundReturned() throws Exception {
        Long groupId = 10L;
        doThrow(new ProductOptionGroupNotFoundException(groupId))
                .when(productOptionGroupService).deactivateOptionGroup(groupId);

        mockMvc.perform(patch("/api/option-groups/{groupId}/deactivate", groupId))
                .andExpect(status().isNotFound());

        verify(productOptionGroupService).deactivateOptionGroup(groupId);
    }

    @Test
    void activateOptionGroup_GroupAndParentProductExist_NoContentReturned() throws Exception {
        Long groupId = 10L;

        mockMvc.perform(patch("/api/option-groups/{groupId}/activate", groupId))
                .andExpect(status().isNoContent());

        verify(productOptionGroupService).activateOptionGroup(groupId);
    }

    @Test
    void activateOptionGroup_ParentProductDoesNotExist_NotFoundReturned() throws Exception {
        Long groupId = 10L;
        doThrow(new ProductNotFoundException(1L))
                .when(productOptionGroupService).activateOptionGroup(groupId);

        mockMvc.perform(patch("/api/option-groups/{groupId}/activate", groupId))
                .andExpect(status().isNotFound());

        verify(productOptionGroupService).activateOptionGroup(groupId);
    }
}
