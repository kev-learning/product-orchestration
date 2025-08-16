package com.microservices.core.product.orchestration.service.dto;

import lombok.Builder;

@Builder
public record RecommendationSummaryDTO(Long recommendationId, String author, Integer rating) {
}
