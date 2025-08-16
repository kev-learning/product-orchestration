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

    public ProductAggregateDTO buildProductAggregate(Long productId) {
        ProductDTO productDTO = productService.getProduct(productId);

        List<ReviewDTO> reviews = reviewService.getReviews(productId);
        List<RecommendationDTO> recommendations = recommendationService.getProductRecommendations(productId);

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
