package com.microservices.core.product.orchestration.service.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.core.product.orchestration.service.mapper.ReviewMapper;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.product.orchestration.service.util.ProductOrchestrationUtil;
import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.dto.ReviewSummaryDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ReviewDTO;
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
public class ReviewService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.service.review.host}")
    private String reviewServiceHost;

    @Value("${app.service.review.port}")
    private Integer reviewServicePort;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    public List<ReviewSummaryDTO> buildReviewSummaries(List<ReviewDTO> reviews) {
        return Optional.ofNullable(reviews).orElse(Collections.emptyList())
                .stream().map(reviewMapper::mapBtoA).toList();
    }

    public List<ReviewDTO> buildReviews(List<ReviewSummaryDTO> reviewSummaryDTOS, ProductDTO productDTO) {
        return Optional.ofNullable(reviewSummaryDTOS).orElse(Collections.emptyList())
                .stream().map(reviewSummaryDTO -> reviewMapper.mapAtoB(reviewSummaryDTO, productDTO)).toList();
    }

    public List<ReviewDTO> getProductReviews(Long productId) {
        log.debug("Retrieving product reviews for product ID: {}", productId);
        log.debug("URL: {}{}", getReviewServiceUrl(),productId);

        try {
            List<ReviewDTO> reviews = restTemplate.exchange(getReviewServiceWithParamUrl() + productId, HttpMethod.GET, null, new ParameterizedTypeReference<List<ReviewDTO>>() {}).getBody();

            log.debug("Found reviews: {}", reviews);
            return reviews;
        }catch (HttpClientErrorException ex) {
            log.warn("Got an error during review retrieval");
            return Collections.emptyList();
        }
    }

    public List<ReviewDTO> createProductReviews(List<ReviewDTO> reviewDTOS) {
        log.debug("Creating new product reviews: {}", reviewDTOS);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<ReviewDTO>> entity = new HttpEntity<>(reviewDTOS, headers);

            List<ReviewDTO> reviewDTOList = restTemplate.exchange(getReviewServiceUrl(), HttpMethod.POST, entity, new ParameterizedTypeReference<List<ReviewDTO>>() {}).getBody();

            log.debug("Created new product reviews: {}", reviewDTOList);
            return reviewDTOList;
        } catch (HttpClientErrorException ex) {
            ProductOrchestrationUtil.handleException(ex, objectMapper);
            return null;
        }
    }

    public void deleteProductReview(Long productId) {
        log.debug("Deleting product reviews using product ID: {}", productId);

        try {
            restTemplate.delete(URI.create(getReviewServiceWithParamUrl() + productId));
        } catch (HttpClientErrorException ex) {
            ProductOrchestrationUtil.handleException(ex, objectMapper);
        }
    }

    private String getReviewServiceWithParamUrl() {
        return "%s?productId=".formatted(getReviewServiceUrl());
    }

    private String getReviewServiceUrl() {
        return "http://%s:%s/review".formatted(reviewServiceHost, reviewServicePort);
    }
}
