package com.microservices.core.product.orchestration.service.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.core.product.orchestration.service.mapper.RecommendationMapper;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.product.orchestration.service.util.ProductOrchestrationUtil;
import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.dto.RecommendationSummaryDTO;
import com.microservices.core.product.orchestration.service.remote.dto.RecommendationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RecommendationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.service.recommendation.host}")
    private String recommendationServiceHost;

    @Value("${app.service.recommendation.port}")
    private Integer recommendationServicePort;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecommendationMapper recommendationMapper;

    public List<RecommendationSummaryDTO> buildRecommendationSummaries(List<RecommendationDTO> recommendations) {
        return Optional.ofNullable(recommendations).orElse(Collections.emptyList())
                .stream().map(recommendationMapper::mapBtoA).toList();
    }

    public List<RecommendationDTO> buildRecommendations(List<RecommendationSummaryDTO> recommendationSummaryDTOS, ProductDTO productDTO) {
        return Optional.ofNullable(recommendationSummaryDTOS).orElse(Collections.emptyList())
                .stream().map(recommendationSummaryDTO -> recommendationMapper.mapAtoB(recommendationSummaryDTO, productDTO)).toList();
    }

    public List<RecommendationDTO> getProductRecommendations(Long productId) {
        log.debug("Retrieving product recommendation for product ID: {}", productId);
        log.debug("URL: {}{}", getRecommendationServiceUrl(),productId);

        try {
            List<RecommendationDTO> recommendations = restTemplate.exchange(getRecommendationServiceUrlWithParam() + productId, HttpMethod.GET, null, new ParameterizedTypeReference<List<RecommendationDTO>>() {}).getBody();

            log.debug("Found recommendations: {}", recommendations);
            return recommendations;
        }catch(HttpClientErrorException ex) {
            log.warn("Got an error during recommendations retrieval: HTTP Status:{}, message: {}", ex.getMessage(), ex.getResponseBodyAsString());
            return Collections.emptyList();
        }
    }

    public List<RecommendationDTO> createProductRecommendations(List<RecommendationDTO> recommendationDTOS) {
        log.debug("Creating new recommendations: {}", recommendationDTOS);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<RecommendationDTO>> entity = new HttpEntity<>(recommendationDTOS, headers);
            List<RecommendationDTO> recommendationDTOList = restTemplate.exchange(getRecommendationServiceUrl(), HttpMethod.POST, entity, new ParameterizedTypeReference<List<RecommendationDTO>>() {}).getBody();

            log.debug("Created recommendations: {}", recommendationDTOList);
            return recommendationDTOList;
        } catch(HttpClientErrorException ex) {
            ProductOrchestrationUtil.handleException(ex, objectMapper);
            return null;
        }
    }

    public void deleteProductRecommendations(Long productId) {
        log.debug("Deleting product recommendation using product ID: {}", productId);

        try {
            restTemplate.delete(URI.create(getRecommendationServiceUrlWithParam() + productId));
        } catch(HttpClientErrorException ex){
            ProductOrchestrationUtil.handleException(ex, objectMapper);
        }
    }

    private String getRecommendationServiceUrlWithParam() {
        return "%s?productId=".formatted(getRecommendationServiceUrl());
    }

    private String getRecommendationServiceUrl() {
        return "http://%s:%s/recommendation".formatted(recommendationServiceHost, recommendationServicePort);
    }
}
