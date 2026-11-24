package com.onlinestore.product_service.controller;

import com.onlinestore.product_service.command.CreateProductOptionCommand;
import com.onlinestore.product_service.command.UpdateProductOptionCommand;
import com.onlinestore.product_service.dto.ProductOptionDto;
import com.onlinestore.product_service.service.ProductOptionGroupService;
import com.onlinestore.product_service.service.ProductOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
@Slf4j
public class ProductOptionController {

    private final ProductOptionService productOptionService;

    @Operation(summary = "Get active options for an option group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Options retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Option group or product not found")
    })
    @GetMapping("/groups/{groupId}")
    public List<ProductOptionDto> getActiveOptions(@PathVariable Long groupId) {
        log.debug("Fetching active options for group with id: {}", groupId);
        return productOptionService.getActiveOptions(groupId);
    }

    @Operation(summary = "Create a product option")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Option created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid option data"),
            @ApiResponse(responseCode = "404", description = "Option group or product not found"),
            @ApiResponse(responseCode = "409", description = "Option code already exists")
    })
    @PostMapping("/groups/{groupId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductOptionDto createOption(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateProductOptionCommand command
    ) {
        log.debug("Creating option for group with id: {}", groupId);
        return productOptionService.createOption(groupId, command);
    }

    @Operation(summary = "Update a product option")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Option updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid option data"),
            @ApiResponse(responseCode = "404", description = "Option, group or product not found"),
            @ApiResponse(responseCode = "409", description = "Option code already exists")
    })
    @PutMapping("/{optionId}")
    public ProductOptionDto updateOption(
            @PathVariable Long optionId,
            @Valid @RequestBody UpdateProductOptionCommand command
    ) {
        log.debug("Updating option with id: {}", optionId);
        return productOptionService.updateOption(optionId, command);
    }

    @Operation(summary = "Deactivate a product option")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Option deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Option not found")
    })
    @PatchMapping("/{optionId}/deactivate")
    public void deactivateOption(@PathVariable Long optionId) {
        log.debug("Deactivating option with id: {}", optionId);
        productOptionService.deactivateOption(optionId);
    }

    @Operation(summary = "Activate a product option")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Option activated successfully"),
            @ApiResponse(responseCode = "404", description = "Option, group or product not found")
    })
    @PatchMapping("/{optionId}/activate")
    public void activateOption(@PathVariable Long optionId) {
        log.debug("Activating option with id: {}", optionId);
        productOptionService.activateOption(optionId);
    }
}