package com.microservices.core.product.orchestration.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductAggregateDTO(@JsonProperty("productId") Long productId, @JsonProperty("name")String name, @JsonProperty("weight")Integer weight, @JsonProperty("recommendationSummaries")List<RecommendationSummaryDTO> recommendationSummaries, @JsonProperty("reviewSummaries")List<ReviewSummaryDTO> reviewSummaries, @JsonProperty("serviceAddresses")ServiceAddressesDTO serviceAddresses, @JsonProperty("warnings")List<String> warnings) {
}
