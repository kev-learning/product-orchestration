package com.microservices.core.product.orchestration.service.remote;

import lombok.extern.slf4j.Slf4j;
import com.microservices.core.product.orchestration.service.dto.ProductAggregateDTO;
import com.microservices.core.product.orchestration.service.dto.RecommendationSummaryDTO;
import com.microservices.core.product.orchestration.service.dto.ReviewSummaryDTO;
import com.microservices.core.product.orchestration.service.dto.ServiceAddressesDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.product.orchestration.service.remote.dto.RecommendationDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ReviewDTO;
import com.microservices.core.util.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

@Slf4j
@Component
public class IntegrationService {

    @Autowired
    private ServiceUtil serviceUtil;

    @Autowired
    private ProductService productService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ReviewService reviewService;

    private final SecurityContext securityContext = new SecurityContextImpl();

    public Flux<ProductAggregateDTO> createProductAggregate(ProductAggregateDTO productAggregateDTO) {
        try {

            ProductDTO productDTO = productService.buildProduct(productAggregateDTO);

            // Create first the Mono and Flux objects and will be in the Flux.zip for execution.
            Mono<ProductDTO> createdProduct = productService.createProduct(productDTO);
            Flux<List<RecommendationDTO>> createdRecommendations = Flux.empty();
            Flux<List<ReviewDTO>> createdReviews = Flux.empty();


            if(!CollectionUtils.isEmpty(productAggregateDTO.recommendationSummaries())) {
                List<RecommendationDTO> recommendations = recommendationService.buildRecommendations(productAggregateDTO.recommendationSummaries(), productDTO);

                createdRecommendations = recommendationService.createProductRecommendations(recommendations);
            }

            if(!CollectionUtils.isEmpty(productAggregateDTO.reviewSummaries())) {
                List<ReviewDTO> reviews = reviewService.buildReviews(productAggregateDTO.reviewSummaries(), productDTO);

                createdReviews = reviewService.createProductReviews(reviews);
            }


            // This will execute the Mono and Flux operations and will wait for all the operations to complete to build the response.
            return Flux.zip(getLogAuthorizationInfoMono(), createdProduct, createdRecommendations, createdReviews)
                    .map (tuple -> buildProductAggregate(tuple.getT1(), tuple.getT2(), tuple.getT4(), tuple.getT3()))
                    .doOnError(ex -> log.warn("Product detail creation failed: {}", ex.getMessage()))
                    .log(log.getName(), Level.FINE);
        }catch (Exception e) {
            //Remove data that were persisted
            productService.deleteProduct(productAggregateDTO.productId());
            recommendationService.deleteProductRecommendations(productAggregateDTO.productId());
            reviewService.deleteProductReview(productAggregateDTO.productId());
            return Flux.empty();
        }
    }

    public Mono<Void> createProductAggregateAsync(ProductAggregateDTO productAggregateDTO) {
        try {

            ProductDTO productDTO = productService.buildProduct(productAggregateDTO);

            List<Mono<?>> monos = new ArrayList<>();

            monos.add(getLogAuthorizationInfoMono());
            monos.add(productService.createProductAsync(productDTO));

            if(!CollectionUtils.isEmpty(productAggregateDTO.reviewSummaries())) {
                List<ReviewDTO> reviewDTOS = reviewService.buildReviews(productAggregateDTO.reviewSummaries(), productDTO);

                reviewDTOS.forEach(reviewDTO -> {
                    monos.add(reviewService.createProductReviewAsync(reviewDTO));
                });
            }

            if(!CollectionUtils.isEmpty(productAggregateDTO.recommendationSummaries())) {
                List<RecommendationDTO> recommendationDTOS = recommendationService.buildRecommendations(productAggregateDTO.recommendationSummaries(), productDTO);

                recommendationDTOS.forEach(recommendationDTO -> {
                    monos.add(recommendationService.createProductRecommendationAsync(recommendationDTO));
                });
            }

            return Mono.zip(r -> "", monos.toArray(new Mono[0]))
                    .doOnError(ex -> log.warn("Creation of product failed: {}", ex.toString()))
                    .then();
        }catch (Exception e) {
            //Remove data that were persisted
            productService.deleteProductAsync(productAggregateDTO.productId());
            recommendationService.deleteProductRecommendationsAsync(productAggregateDTO.productId());
            reviewService.deleteProductReviewAsync(productAggregateDTO.productId());
            throw e;
        }
    }

    public Mono<Void> deleteProductAggregate(Long productId) {
        try {
            return Mono.zip(execution -> "",
                    getLogAuthorizationInfoMono(),
                    productService.deleteProduct(productId),
                    recommendationService.deleteProductRecommendations(productId),
                    reviewService.deleteProductReview(productId))
                    .doOnError(ex -> log.warn("Product deletion failed: {}", ex.getMessage()))
                    .log(log.getName(), Level.FINE).then();
        }catch(RuntimeException ex) {
            throw ex;
        }
    }

    public Mono<Void> deleteProductAggregateAsync(Long productId) {
        return Mono.zip(r -> "",
                getLogAuthorizationInfoMono(),
                productService.deleteProductAsync(productId),
                recommendationService.deleteProductRecommendationsAsync(productId),
                reviewService.deleteProductReviewAsync(productId))
                .doOnError(ex -> log.warn("Deletion of product failed: {}", ex.getMessage()))
                .log(log.getName(), Level.FINE).then();
    }

    public Mono<ProductAggregateDTO> getProductAggregate(Long productId) {


        return Mono.zip(values -> buildProductAggregate((SecurityContext) values[0], (ProductDTO) values[1], (List<ReviewDTO>) values[2], (List<RecommendationDTO>) values[3]),
                        getLogAuthorizationInfoMono(), productService.getProduct(productId), reviewService.getProductReviews(productId).collectList(), recommendationService.getProductRecommendations(productId).collectList())
                .doOnError(ex -> log.warn("Product detail retrieval failed: {}", ex.getMessage()))
                .log(log.getName(), Level.FINE);
    }

    private ProductAggregateDTO buildProductAggregate(SecurityContext securityContext, ProductDTO productDTO, List<ReviewDTO> reviews, List<RecommendationDTO> recommendations) {
        String productServiceAddress = productDTO.getServiceAddress();
        String reviewServiceAddress = "";
        String recommendationServiceAddress = "";

        logAuthorizationInfo(securityContext);

        List<String> warnings = new ArrayList<>();

        if(CollectionUtils.isEmpty(reviews)) {
            warnings.add("Empty product review");
        } else {
            reviewServiceAddress = reviews.get(0).serviceAddress();
        }

        if(CollectionUtils.isEmpty(recommendations)) {
            warnings.add("Empty product recommendations");
        } else {
            recommendationServiceAddress = recommendations.get(0).serviceAddress();
        }

        List<ReviewSummaryDTO> reviewSummaries = reviewService.buildReviewSummaries(reviews);
        List<RecommendationSummaryDTO> recommendationSummaries = recommendationService.buildRecommendationSummaries(recommendations);

        ServiceAddressesDTO serviceAddressesDTO = ServiceAddressesDTO.builder()
                .productAddress(productServiceAddress)
                .reviewAddress(reviewServiceAddress)
                .recommendationAddress(recommendationServiceAddress)
                .orchestrationAddress(serviceUtil.getAddress())
                .build();

        return ProductAggregateDTO.builder()
                .productId(productDTO.getProductId())
                .name(productDTO.getName())
                .weight(productDTO.getWeight())
                .recommendationSummaries(recommendationSummaries)
                .reviewSummaries(reviewSummaries)
                .serviceAddresses(serviceAddressesDTO)
                .warnings(warnings)
                .build();
    }

    private Mono<SecurityContext> getLogAuthorizationInfoMono() {
        return getSecurityContextMono().doOnNext(this::logAuthorizationInfo);
    }

    private Mono<SecurityContext> getSecurityContextMono() {
        return ReactiveSecurityContextHolder.getContext().defaultIfEmpty(securityContext);
    }

    private void logAuthorizationInfo(SecurityContext securityContext) {
        if(Objects.nonNull(securityContext) && Objects.nonNull(securityContext.getAuthentication()) && securityContext.getAuthentication() instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) securityContext.getAuthentication()).getToken();
            logAuthorizationInfo(jwt);
        } else {
            log.warn("No JWT based authentication supplied");
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if(Objects.isNull(jwt)) {
            log.warn("No JWT supplied.");
        }else {
            URL issuer = jwt.getIssuer();
            List<String> audience = jwt.getAudience();
            Object subject = jwt.getClaims().get("sub");
            Object scopes = jwt.getClaims().get("scope");
            Object expires = jwt.getClaims().get("exp");

            log.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject, scopes, expires, issuer, audience);
        }
    }
}
