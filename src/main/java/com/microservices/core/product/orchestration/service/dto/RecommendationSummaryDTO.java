package com.microservices.core.product.orchestration.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record RecommendationSummaryDTO(@JsonProperty("recommendationId")Long recommendationId, @JsonProperty("author")String author, @JsonProperty("rating")Integer rating, @JsonProperty("content")String content) {
}
