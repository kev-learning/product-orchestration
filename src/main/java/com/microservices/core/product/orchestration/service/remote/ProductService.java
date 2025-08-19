package com.microservices.core.product.orchestration.service.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.core.product.orchestration.service.dto.ProductAggregateDTO;
import com.microservices.core.product.orchestration.service.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.util.ProductOrchestrationUtil;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.logging.Level;

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

    public Mono<ProductDTO> getProduct(Long productId) {
        log.debug("Retrieving product information using ID: {}", productId);
        log.debug("URL: {}{}", getProductServiceUrl(),productId);

        WebClient webClient = WebClient.builder().build();

        return webClient.get()
                .uri(getProductServiceUrl() + productId)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .log(log.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> ProductOrchestrationUtil.handleWebClientException(ex, objectMapper));

    }

    public Mono<ProductDTO> createProduct(ProductDTO productDTO) {
        log.debug("Creating new product: {}", productDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ProductDTO> entity = new HttpEntity<>(productDTO, headers);

        WebClient webClient = WebClient.builder().build();

        return webClient.post()
                .uri(getProductServiceUrl())
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .body(Mono.just(productDTO), ProductDTO.class)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .log(log.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> ProductOrchestrationUtil.handleWebClientException(ex, objectMapper));


    }

    public Mono<Void> deleteProduct(Long productId) {
        log.debug("Deleting product using ID: {}", productId);

        WebClient webClient = WebClient.builder().build();
        return webClient.delete()
                .uri(getProductServiceUrl() + productId)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, ex -> ProductOrchestrationUtil.handleWebClientException(ex, objectMapper));

    }

    private String getProductServiceUrl() {
        return "http://%s:%s/product/".formatted(productServiceHost, productServicePort);
    }
}
