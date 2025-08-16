package com.microservices.core.product.orchestration.service.remote.dto;

import lombok.Builder;

@Builder
public record RecommendationDTO(Long recommendationId, Long productId, String author, Integer rating, String content, String serviceAddress) {
}
