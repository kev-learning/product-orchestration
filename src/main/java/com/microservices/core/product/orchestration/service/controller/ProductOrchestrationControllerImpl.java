package com.microservices.core.product.orchestration.service.controller;

import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.remote.IntegrationService;
import com.microservices.core.util.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Slf4j
@RestController
public class ProductOrchestrationControllerImpl implements ProductOrchestrationController{

    @Autowired
    private IntegrationService integrationService;

    @Override
    public ResponseEntity<Object> getAggregatedProductDetails(Long productId) {

        if(Objects.isNull(productId) || productId < 1) {
            throw new NotFoundException("No product found for ID: %s".formatted(productId));
        }

        return ResponseEntity.ok(integrationService.buildProductAggregate(productId));
    }
}
