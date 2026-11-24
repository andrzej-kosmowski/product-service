
package com.onlinestore.product_service.controller;

import com.onlinestore.product_service.command.CreateProductOptionCommand;
import com.onlinestore.product_service.command.UpdateProductOptionCommand;
import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.exception.*;
import com.onlinestore.product_service.service.ProductOptionService;
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

@WebMvcTest(ProductOptionController.class)
@Import(ProductServiceExceptionHandler.class)
class ProductOptionControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductOptionService productOptionService;

    @Test
    void getActiveOptions_OptionsExist_OptionsReturned() throws Exception {
        // given
        Long groupId = 1L;
        ProductOptionDto option = new ProductOptionDto(
                10L,
                "16 GB",
                "RAM_16",
                "16 GB",
                null
        );
        when(productOptionService.getActiveOptions(groupId))
                .thenReturn(List.of(option));
        // when & then
        mockMvc.perform(get("/api/options/groups/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("16 GB"))
                .andExpect(jsonPath("$[0].code").value("RAM_16"))
                .andExpect(jsonPath("$[0].value").value("16 GB"));
        verify(productOptionService).getActiveOptions(groupId);
    }

    @Test
    void getActiveOptions_GroupDoesNotExist_NotFoundReturned() throws Exception {
        // given
        Long groupId = 1L;
        when(productOptionService.getActiveOptions(groupId))
                .thenThrow(new ProductOptionGroupNotFoundException(groupId));
        // when & then
        mockMvc.perform(get("/api/options/groups/{groupId}", groupId))
                .andExpect(status().isNotFound());
        verify(productOptionService).getActiveOptions(groupId);
    }

    @Test
    void getActiveOptions_ProductDoesNotExist_NotFoundReturned() throws Exception {
        // given
        Long groupId = 1L;
        Long productId = 10L;
        when(productOptionService.getActiveOptions(groupId))
                .thenThrow(new ProductNotFoundException(productId));
        // when & then
        mockMvc.perform(get("/api/options/groups/{groupId}", groupId))
                .andExpect(status().isNotFound());
        verify(productOptionService).getActiveOptions(groupId);
    }

    @Test
    void createOption_ValidCommand_OptionCreated() throws Exception {
        // given
        CreateProductOptionCommand command = new CreateProductOptionCommand(
                "RAM 16 GB",
                "RAM_16",
                "16 GB",
                null
        );
        ProductOptionDto optionDto = new ProductOptionDto(
                100L,
                "RAM 16 GB",
                "RAM_16",
                "16 GB",
                null
        );
        when(productOptionService.createOption(eq(10L), any(CreateProductOptionCommand.class)))
                .thenReturn(optionDto);
        // when & then
        mockMvc.perform(post("/api/options/groups/{groupId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.name").value("RAM 16 GB"))
                .andExpect(jsonPath("$.code").value("RAM_16"))
                .andExpect(jsonPath("$.value").value("16 GB"));
        verify(productOptionService).createOption(eq(10L), any(CreateProductOptionCommand.class));
    }

    @Test
    void createOption_InvalidCommand_BadRequestReturned() throws Exception {
        // given
        CreateProductOptionCommand command = new CreateProductOptionCommand(
                "",
                "",
                "16 GB",
                null
        );
        // when & then
        mockMvc.perform(post("/api/options/groups/{groupId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productOptionService);
    }

    @Test
    void createOption_GroupNotFound_NotFoundReturned() throws Exception {
        // given
        CreateProductOptionCommand command = new CreateProductOptionCommand(
                "RAM 16 GB",
                "RAM_16",
                "16 GB",
                null
        );
        when(productOptionService.createOption(eq(10L), any(CreateProductOptionCommand.class)))
                .thenThrow(new ProductOptionGroupNotFoundException(10L));
        // when & then
        mockMvc.perform(post("/api/options/groups/{groupId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound());
        verify(productOptionService).createOption(eq(10L), any(CreateProductOptionCommand.class));
    }

    @Test
    void createOption_CodeAlreadyExists_ConflictReturned() throws Exception {
        // given
        CreateProductOptionCommand command = new CreateProductOptionCommand(
                "RAM 16 GB",
                "RAM_16",
                "16 GB",
                null
        );
        when(productOptionService.createOption(eq(10L), any(CreateProductOptionCommand.class)))
                .thenThrow(new ProductOptionAlreadyExistsException(10L, "RAM_16"));
        // when & then
        mockMvc.perform(post("/api/options/groups/{groupId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isConflict());
        verify(productOptionService).createOption(eq(10L), any(CreateProductOptionCommand.class));
    }


    @Test
    void updateOption_ValidCommand_OptionUpdated() throws Exception {
        // given
        UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                "RAM 32 GB",
                "RAM_32",
                "32 GB",
                null
        );
        ProductOptionDto updatedOption = new ProductOptionDto(
                100L,
                "RAM 32 GB",
                "RAM_32",
                "32 GB",
                null
        );
        when(productOptionService.updateOption(eq(100L), any(UpdateProductOptionCommand.class)))
                .thenReturn(updatedOption);
        // when & then
        mockMvc.perform(put("/api/options/{optionId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.name").value("RAM 32 GB"))
                .andExpect(jsonPath("$.code").value("RAM_32"))
                .andExpect(jsonPath("$.value").value("32 GB"));
        verify(productOptionService).updateOption(eq(100L), any(UpdateProductOptionCommand.class));
    }

    @Test
    void updateOption_InvalidCommand_BadRequestReturned() throws Exception {
        // given
        UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                "",
                "",
                "32 GB",
                null
        );
        // when & then
        mockMvc.perform(put("/api/options/{optionId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productOptionService);
    }

    @Test
    void updateOption_OptionNotFound_NotFoundReturned() throws Exception {
        // given
        UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                "RAM 32 GB",
                "RAM_32",
                "32 GB",
                null
        );
        when(productOptionService.updateOption(eq(100L), any(UpdateProductOptionCommand.class)))
                .thenThrow(new ProductOptionNotFoundException(100L));
        // when & then
        mockMvc.perform(put("/api/options/{optionId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound());
        verify(productOptionService).updateOption(eq(100L), any(UpdateProductOptionCommand.class));
    }

    @Test
    void updateOption_CodeAlreadyExists_ConflictReturned() throws Exception {
        // given
        UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                "RAM 32 GB",
                "RAM_32",
                "32 GB",
                null
        );
        when(productOptionService.updateOption(eq(100L), any(UpdateProductOptionCommand.class)))
                .thenThrow(new ProductOptionAlreadyExistsException(10L, "RAM_32"));
        // when & then
        mockMvc.perform(put("/api/options/{optionId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isConflict());
        verify(productOptionService).updateOption(eq(100L), any(UpdateProductOptionCommand.class));
    }

    @Test
    void deactivateOption_OptionExists_OptionDeactivated() throws Exception {
        // given
        doNothing().when(productOptionService).deactivateOption(100L);
        // when & then
        mockMvc.perform(patch("/api/options/{optionId}/deactivate", 100L))
                .andExpect(status().isOk());
        verify(productOptionService).deactivateOption(100L);
    }

    @Test
    void deactivateOption_OptionNotFound_NotFoundReturned() throws Exception {
        // given
        doThrow(new ProductOptionNotFoundException(100L))
                .when(productOptionService).deactivateOption(100L);
        // when & then
        mockMvc.perform(patch("/api/options/{optionId}/deactivate", 100L))
                .andExpect(status().isNotFound());
        verify(productOptionService).deactivateOption(100L);
    }

    @Test
    void activateOption_OptionAndParentsActive_OptionActivated() throws Exception {
        // given
        doNothing().when(productOptionService).activateOption(100L);
        // when & then
        mockMvc.perform(patch("/api/options/{optionId}/activate", 100L))
                .andExpect(status().isOk());
        verify(productOptionService).activateOption(100L);
    }

    @Test
    void activateOption_GroupNotFound_NotFoundReturned() throws Exception {
        // given
        doThrow(new ProductOptionGroupNotFoundException(10L))
                .when(productOptionService).activateOption(100L);
        // when & then
        mockMvc.perform(patch("/api/options/{optionId}/activate", 100L))
                .andExpect(status().isNotFound());
        verify(productOptionService).activateOption(100L);
    }

}
