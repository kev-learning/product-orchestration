package com.microservices.core.product.orchestration.service.controller;

import com.microservices.core.product.orchestration.service.dto.ProductAggregateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.remote.IntegrationService;
import com.microservices.core.util.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@Tag(name = "Product Orchestration", description = "REST API for operations on Product and it's related data.")
public class ProductOrchestrationController {

    @Autowired
    private IntegrationService integrationService;

    @Operation(summary = "Returns the product details and associated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Provided Product ID is invalid"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/product-orchestration/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ProductAggregateDTO> getAggregatedProductDetails(@PathVariable("productId") Long productId) {

        if(Objects.isNull(productId) || productId < 1) {
            throw new NotFoundException("No product found for ID: %s".formatted(productId));
        }

        return ResponseEntity.ok(integrationService.getProductAggregate(productId));
    }

    @Operation(summary = "Create the product and associated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Provided Product ID is invalid"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(value = "/product-orchestration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ProductAggregateDTO> createProductAggregate(@RequestBody ProductAggregateDTO productAggregateDTO) {
        return new ResponseEntity<>(integrationService.createProductAggregate(productAggregateDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Delete the product and associated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Provided Product ID is invalid"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @DeleteMapping(value = "/product-orchestration/{productId}")
    void deleteProductAggregate(@PathVariable("productId") Long productId) {
        integrationService.deleteProductAggregate(productId);
    }
}
