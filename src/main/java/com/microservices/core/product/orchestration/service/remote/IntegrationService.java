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
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
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
            return Flux.zip(createdProduct, createdRecommendations, createdReviews)
                    .map (tuple -> buildProductAggregate(tuple.getT1(), tuple.getT3(), tuple.getT2()))
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

    public Mono<Void> deleteProductAggregate(Long productId) {
        try {
            return Mono.zip(execution -> "",
                    productService.deleteProduct(productId),
                    recommendationService.deleteProductRecommendations(productId),
                    reviewService.deleteProductReview(productId))
                    .doOnError(ex -> log.warn("Product deletion failed: {}", ex.getMessage()))
                    .log(log.getName(), Level.FINE).then();
        }catch(RuntimeException ex) {
            throw ex;
        }
    }

    public Mono<ProductAggregateDTO> getProductAggregate(Long productId) {


        return Mono.zip(values -> buildProductAggregate((ProductDTO) values[0], (List<ReviewDTO>) values[1], (List<RecommendationDTO>) values[2]),
            productService.getProduct(productId), reviewService.getProductReviews(productId).collectList(), recommendationService.getProductRecommendations(productId).collectList())
                .doOnError(ex -> log.warn("Product detail retrieval failed: {}", ex.getMessage()))
                .log(log.getName(), Level.FINE);
    }

    private ProductAggregateDTO buildProductAggregate(ProductDTO productDTO, List<ReviewDTO> reviews, List<RecommendationDTO> recommendations) {
        String productServiceAddress = productDTO.getServiceAddress();
        String reviewServiceAddress = "";
        String recommendationServiceAddress = "";

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
}
