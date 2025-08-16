package com.microservices.core.product.orchestration.service.remote;

import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.dto.ReviewSummaryDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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

    public List<ReviewSummaryDTO> buildReviewSummaries(List<ReviewDTO> reviews) {
        return Optional.ofNullable(reviews).orElse(Collections.emptyList())
                .stream().map(review -> ReviewSummaryDTO.builder()
                        .reviewId(review.reviewId())
                        .author(review.author())
                        .subject(review.subject())
                        .build()).toList();
    }

    public List<ReviewDTO> getReviews(Long productId) {
        log.debug("Retrieving product reviews for product ID: {}", productId);
        log.debug("URL: {}{}", getReviewServiceUrl(),productId);

        try {
            List<ReviewDTO> reviews = restTemplate.exchange(getReviewServiceUrl() + productId, HttpMethod.GET, null, new ParameterizedTypeReference<List<ReviewDTO>>() {}).getBody();

            log.debug("Found reviews: {}", reviews);
            return reviews;
        }catch (HttpClientErrorException ex) {
            log.warn("Got an error during review retrieval");
            return Collections.emptyList();
        }
    }

    private String getReviewServiceUrl() {
        return "http://%s:%s/review?productId=".formatted(reviewServiceHost, reviewServicePort);
    }
}
