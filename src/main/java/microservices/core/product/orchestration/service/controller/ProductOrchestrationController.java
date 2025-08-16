package microservices.core.product.orchestration.service.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductOrchestrationController {

    @GetMapping(value = "/product-orchestration/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getAggregatedProductDetails(@PathVariable("productId") Long productId);
}
