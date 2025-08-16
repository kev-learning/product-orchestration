package com.microservices.core.product.orchestration.service.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.util.ProductOrchestrationUtil;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.util.exceptions.InvalidInputException;
import com.microservices.core.util.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
public class ProductService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.service.product.host}")
    private String productServiceHost;

    @Value("${app.service.product.port}")
    private Integer productServicePort;

    public ProductDTO getProduct(Long productId) {
        log.debug("Retrieving product information using ID: {}", productId);
        log.debug("URL: {}{}", getProductServiceUrl(),productId);

        try {
            ProductDTO productDTO = restTemplate.getForObject(getProductServiceUrl() + productId, ProductDTO.class);

            log.debug("Product found: {}", productDTO);
            return productDTO;
        }catch(HttpClientErrorException ex) {
            switch (Optional.ofNullable(HttpStatus.resolve(ex.getStatusCode().value())).orElse(HttpStatus.INTERNAL_SERVER_ERROR)) {
                case NOT_FOUND -> throw new NotFoundException(ProductOrchestrationUtil.getErrorMessage(objectMapper, ex));
                case BAD_REQUEST -> throw new InvalidInputException(ProductOrchestrationUtil.getErrorMessage(objectMapper, ex));
                default -> {
                    log.warn("Unexpected error: {} with response body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                    throw ex;
                }
            }
        }
    }

    private String getProductServiceUrl() {
        return "http://%s:%s/product/".formatted(productServiceHost, productServicePort);
    }
}
