package com.microservices.core.product.orchestration.service.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.core.product.orchestration.service.mapper.RecommendationMapper;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.product.orchestration.service.util.ProductOrchestrationUtil;
import com.microservices.core.product.orchestration.service.util.TopicConstants;
import com.microservices.core.util.api.event.Event;
import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.dto.RecommendationSummaryDTO;
import com.microservices.core.product.orchestration.service.remote.dto.RecommendationDTO;
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

    @Autowired
    @Qualifier("publishEventScheduler")
    private Scheduler publishEventScheduler;

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private WebClient webClient;

    public List<RecommendationSummaryDTO> buildRecommendationSummaries(List<RecommendationDTO> recommendations) {
        return Optional.ofNullable(recommendations).orElse(Collections.emptyList())
                .stream().map(recommendationMapper::mapBtoA).toList();
    }

    public List<RecommendationDTO> buildRecommendations(List<RecommendationSummaryDTO> recommendationSummaryDTOS, ProductDTO productDTO) {
        return Optional.ofNullable(recommendationSummaryDTOS).orElse(Collections.emptyList())
                .stream().map(recommendationSummaryDTO -> recommendationMapper.mapAtoB(recommendationSummaryDTO, productDTO)).toList();
    }

    public Flux<RecommendationDTO> getProductRecommendations(Long productId) {
        log.debug("Retrieving product recommendation for product ID: {}", productId);
        log.debug("URL: {}{}", getRecommendationServiceUrlWithParam(),productId);

        Flux<RecommendationDTO> recommendationDTOFlux = webClient.get()
                .uri(getRecommendationServiceUrlWithParam() + productId)
                .retrieve()
                .bodyToFlux(RecommendationDTO.class)
                .log(log.getName(), Level.FINE)
                .onErrorResume(error -> {
                    log.debug("Encountered and error: {}", error.getMessage());
                    return Flux.empty();
                });

        log.debug("Found recommendations: {}", recommendationDTOFlux);
        return recommendationDTOFlux;
    }

    public Mono<RecommendationDTO> createProductRecommendationAsync(RecommendationDTO recommendationDTO) {
        return Mono.fromCallable(() -> {
            ProductOrchestrationUtil.sendMessage(streamBridge, new Event<>(Event.Type.CREATE, recommendationDTO.productId(), recommendationDTO, ZonedDateTime.now()), TopicConstants.RECOMMENDATION_TOPIC);
            return recommendationDTO;
        }).subscribeOn(publishEventScheduler);
    }

    public Flux<List<RecommendationDTO>> createProductRecommendations(List<RecommendationDTO> recommendationDTOS) {
        if(CollectionUtils.isEmpty(recommendationDTOS)) {
            return Flux.empty();
        }

        log.debug("Creating new recommendations: {}", recommendationDTOS);

        Flux<List<RecommendationDTO>> recommendationDTOFlux = webClient.post()
                .uri(getRecommendationServiceUrl())
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .body(Flux.fromIterable(recommendationDTOS), RecommendationDTO.class)
                .retrieve()
                .bodyToFlux(RecommendationDTO.class)
                .onErrorMap(WebClientResponseException.class, ex -> ProductOrchestrationUtil.handleWebClientException(ex, objectMapper))
                .collectList().flatMapMany(Flux::just);

        log.debug("Created recommendations: {}", recommendationDTOFlux);
        return recommendationDTOFlux;
    }

    public Mono<Void> deleteProductRecommendationsAsync(Long productId) {
        return Mono.fromRunnable(() -> {
            ProductOrchestrationUtil.sendMessage(streamBridge, new Event<>(Event.Type.DELETE, productId, null, null), TopicConstants.RECOMMENDATION_TOPIC);
        }).subscribeOn(publishEventScheduler).then();
    }

    public Mono<Void> deleteProductRecommendations(Long productId) {
        log.debug("Deleting product recommendation using product ID: {}", productId);

        return webClient.delete()
                .uri(getRecommendationServiceUrlWithParam() + productId)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, ex -> ProductOrchestrationUtil.handleWebClientException(ex, objectMapper));

    }

    private String getRecommendationServiceUrlWithParam() {
        return "%s?productId=".formatted(getRecommendationServiceUrl());
    }

    private String getRecommendationServiceUrl() {
        return "http://recommendation-service/recommendation";
    }
}
