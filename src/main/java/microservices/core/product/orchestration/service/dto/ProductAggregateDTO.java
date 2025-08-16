package microservices.core.product.orchestration.service.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductAggregateDTO(Long productId, String name, Integer weight, List<RecommendationSummaryDTO> recommendationSummaries, List<ReviewSummaryDTO> reviewSummaries, ServiceAddressesDTO serviceAddresses, List<String> warnings) {
}
