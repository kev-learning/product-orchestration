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

import java.util.ArrayList;
import java.util.List;

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

    public ProductAggregateDTO createProductAggregate(ProductAggregateDTO productAggregateDTO) {
        try {

            ProductDTO productDTO = productService.buildProduct(productAggregateDTO);

            ProductDTO createdProduct = productService.createProduct(productDTO);

            List<RecommendationDTO> createdRecommendations = new ArrayList<>();
            List<ReviewDTO> createdReviews = new ArrayList<>();

            if(!CollectionUtils.isEmpty(productAggregateDTO.recommendationSummaries())) {
                List<RecommendationDTO> recommendations = recommendationService.buildRecommendations(productAggregateDTO.recommendationSummaries(), createdProduct);

                createdRecommendations = recommendationService.createProductRecommendations(recommendations);
            }

            if(!CollectionUtils.isEmpty(productAggregateDTO.reviewSummaries())) {
                List<ReviewDTO> reviews = reviewService.buildReviews(productAggregateDTO.reviewSummaries(), createdProduct);

                createdReviews = reviewService.createProductReviews(reviews);
            }

            return buildProductAggregate(createdProduct, createdReviews, createdRecommendations);
        }catch (Exception e) {
            //Remove data that were persisted
            productService.deleteProduct(productAggregateDTO.productId());
            recommendationService.deleteProductRecommendations(productAggregateDTO.productId());
            reviewService.deleteProductReview(productAggregateDTO.productId());
            throw e;
        }
    }

    public void deleteProductAggregate(Long productId) {
        productService.deleteProduct(productId);
        recommendationService.deleteProductRecommendations(productId);
        reviewService.deleteProductReview(productId);
    }

    public ProductAggregateDTO getProductAggregate(Long productId) {
        ProductDTO productDTO = productService.getProduct(productId);

        List<ReviewDTO> reviews = reviewService.getProductReviews(productId);
        List<RecommendationDTO> recommendations = recommendationService.getProductRecommendations(productId);

        return buildProductAggregate(productDTO, reviews, recommendations);
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
