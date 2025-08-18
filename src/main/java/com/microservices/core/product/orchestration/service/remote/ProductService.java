package com.microservices.core.product.orchestration.service.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.core.product.orchestration.service.dto.ProductAggregateDTO;
import com.microservices.core.product.orchestration.service.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.util.ProductOrchestrationUtil;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.util.exceptions.InvalidInputException;
import com.microservices.core.util.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
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

    @Autowired
    private ProductMapper productMapper;

    public ProductDTO buildProduct(ProductAggregateDTO productAggregateDTO) {
        return productMapper.mapAtoB(productAggregateDTO);
    }

    public ProductDTO getProduct(Long productId) {
        log.debug("Retrieving product information using ID: {}", productId);
        log.debug("URL: {}{}", getProductServiceUrl(),productId);

        try {
            ProductDTO productDTO = restTemplate.getForObject(getProductServiceUrl() + productId, ProductDTO.class);

            log.debug("Product found: {}", productDTO);
            return productDTO;
        }catch(HttpClientErrorException ex) {
            ProductOrchestrationUtil.handleException(ex, objectMapper);
            return null;
        }
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        log.debug("Creating new product: {}", productDTO);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ProductDTO> entity = new HttpEntity<>(productDTO, headers);

            ProductDTO createdProduct = restTemplate.postForObject(getProductServiceUrl(), entity, ProductDTO.class);
            log.debug("Created new product: {}", productDTO);
            return createdProduct;
        }catch (HttpClientErrorException ex) {
            ProductOrchestrationUtil.handleException(ex, objectMapper);
            return null;
        }
    }

    public void deleteProduct(Long productId) {
        log.debug("Deleting product using ID: {}", productId);

        try {
            restTemplate.delete(URI.create(getProductServiceUrl() + productId));
        } catch(HttpClientErrorException ex) {
            ProductOrchestrationUtil.handleException(ex, objectMapper);
        }
    }

    private String getProductServiceUrl() {
        return "http://%s:%s/product/".formatted(productServiceHost, productServicePort);
    }
}
