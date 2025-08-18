package com.microservices.core.product.orchestration.service.remote.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record RecommendationDTO(@JsonProperty("recommendationId")Long recommendationId, @JsonProperty("productId")Long productId, @JsonProperty("author")String author, @JsonProperty("rating")Integer rating, @JsonProperty("content")String content, @JsonProperty("serviceAddress")String serviceAddress) {
}
