package microservices.core.product.orchestration.service.remote.dto;

import lombok.Builder;

@Builder
public record ReviewDTO(Long reviewId, Long productId, String author, String subject, String content, String serviceAddress) {

}
