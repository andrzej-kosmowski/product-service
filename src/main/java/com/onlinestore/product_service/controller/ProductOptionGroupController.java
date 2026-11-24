package com.onlinestore.product_service.controller;

import com.onlinestore.product_service.command.CreateProductOptionGroupCommand;
import com.onlinestore.product_service.command.UpdateProductOptionGroupCommand;
import com.onlinestore.product_service.dto.ProductOptionGroupDto;
import com.onlinestore.product_service.service.ProductOptionGroupService;
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
@RequestMapping("/api/option-groups")
@RequiredArgsConstructor
@Slf4j
public class ProductOptionGroupController {
    private final ProductOptionGroupService productOptionGroupService;

    @GetMapping
    @Operation(summary = "Get active option groups for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Option groups retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public List<ProductOptionGroupDto> getActiveOptionGroups(@RequestParam("productId") Long productId) {
        log.info("Fetching active option groups for product with id: {}", productId);
        return productOptionGroupService.getActiveOptionGroups(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an option group for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Option group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid option group data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Option group code already exists")
    })
    public ProductOptionGroupDto createOptionGroup(@RequestParam("productId") Long productId,
            @Valid @RequestBody CreateProductOptionGroupCommand command) {
        log.info("Creating option group for product with id: {}", productId);
        return productOptionGroupService.createProductOptionGroup(productId, command);
    }

    @Operation(summary = "Update an option group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Option group updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid option group data"),
            @ApiResponse(responseCode = "404", description = "Option group or product not found"),
            @ApiResponse(responseCode = "409", description = "Option group code already exists")
    })
    @PutMapping("/{groupId}")
    public ProductOptionGroupDto updateOptionGroup(@PathVariable Long groupId,
                   @Valid @RequestBody UpdateProductOptionGroupCommand command) {
        log.info("Updating option group with id: {}", groupId);
        return productOptionGroupService.updateOptionGroup(groupId, command);
    }
}
