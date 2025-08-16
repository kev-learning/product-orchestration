package microservices.core.product.orchestration.service.dto;

import lombok.Builder;

@Builder
public record ReviewSummaryDTO(Long reviewId, String author, String subject) {
}
