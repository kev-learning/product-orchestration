package com.microservices.core.product.orchestration.service.controller;

import com.microservices.core.product.orchestration.service.dto.ProductAggregateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Product Orchestration", description = "REST API for operations on Product and it's related data.")
public interface ProductOrchestrationController {

    @Operation(summary = "Returns the product details and associated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Provided Product ID is invalid"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/product-orchestration/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ProductAggregateDTO> getAggregatedProductDetails(@PathVariable("productId") Long productId);
}
