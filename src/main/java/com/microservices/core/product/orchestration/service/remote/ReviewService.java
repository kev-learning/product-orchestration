package com.microservices.core.product.orchestration.service.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.core.product.orchestration.service.mapper.ReviewMapper;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.product.orchestration.service.util.ProductOrchestrationUtil;
import com.microservices.core.product.orchestration.service.util.TopicConstants;
import com.microservices.core.util.api.event.Event;
import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.dto.ReviewSummaryDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

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

    @Autowired
    @Qualifier("publishEventScheduler")
    private Scheduler publishEventScheduler;

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private WebClient webClient;

    public List<ReviewSummaryDTO> buildReviewSummaries(List<ReviewDTO> reviews) {
        return Optional.ofNullable(reviews).orElse(Collections.emptyList())
                .stream().map(reviewMapper::mapBtoA).toList();
    }

    public List<ReviewDTO> buildReviews(List<ReviewSummaryDTO> reviewSummaryDTOS, ProductDTO productDTO) {
        return Optional.ofNullable(reviewSummaryDTOS).orElse(Collections.emptyList())
                .stream().map(reviewSummaryDTO -> reviewMapper.mapAtoB(reviewSummaryDTO, productDTO)).toList();
    }

    public Flux<ReviewDTO> getProductReviews(Long productId) {
        log.debug("Retrieving product reviews for product ID: {}", productId);
        log.debug("URL: {}{}", getReviewServiceWithParamUrl(),productId);

        Flux<ReviewDTO> reviewDTOFlux = webClient.get()
                .uri(getReviewServiceWithParamUrl() + productId)
                .retrieve()
                .bodyToFlux(ReviewDTO.class)
                .log(log.getName(), Level.FINE)
                .onErrorResume(error -> {
                    log.debug("Encountered and error: {}", error.getMessage());
                    return Flux.empty();
                });

        log.debug("Found reviews: {}", reviewDTOFlux);
        return reviewDTOFlux;
    }

    public Mono<ReviewDTO> createProductReviewAsync(ReviewDTO reviewDTO) {
        return Mono.fromCallable(() -> {
            ProductOrchestrationUtil.sendMessage(streamBridge, new Event<>(Event.Type.CREATE, reviewDTO.productId(), reviewDTO, ZonedDateTime.now()), TopicConstants.REVIEW_TOPIC);
            return reviewDTO;
        }).subscribeOn(publishEventScheduler);
    }

    public Flux<List<ReviewDTO>> createProductReviews(List<ReviewDTO> reviewDTOS) {
        if(CollectionUtils.isEmpty(reviewDTOS)) {
            return Flux.empty();
        }

        log.debug("Creating new product reviews: {}", reviewDTOS);

        Flux<List<ReviewDTO>> createdReviewDTO =  webClient.post()
                .uri(getReviewServiceUrl())
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .body(Flux.fromIterable(reviewDTOS), ReviewDTO.class)
                .retrieve()
                .bodyToFlux(ReviewDTO.class)
                .onErrorMap(WebClientResponseException.class, ex -> ProductOrchestrationUtil.handleWebClientException(ex, objectMapper))
                .collectList().flatMapMany(Flux::just);

        log.debug("Created new product reviews: {}", createdReviewDTO);
        return createdReviewDTO;

    }

    public Mono<Void> deleteProductReviewAsync(Long productId) {
        return Mono.fromRunnable(() -> {
            ProductOrchestrationUtil.sendMessage(streamBridge, new Event<>(Event.Type.DELETE, productId, null, null), TopicConstants.REVIEW_TOPIC);
        }).subscribeOn(publishEventScheduler).then();
    }

    public Mono<Void> deleteProductReview(Long productId) {
        log.debug("Deleting product reviews using product ID: {}", productId);

        return webClient.delete()
                .uri(getReviewServiceWithParamUrl() + productId)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, ex -> ProductOrchestrationUtil.handleWebClientException(ex, objectMapper));

    }

    private String getReviewServiceWithParamUrl() {
        return "%s?productId=".formatted(getReviewServiceUrl());
    }

    private String getReviewServiceUrl() {
        return "http://review-service/review";
    }
}
