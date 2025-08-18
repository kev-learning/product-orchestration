package com.microservices.core.product.orchestration.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ReviewSummaryDTO(@JsonProperty("reviewId")Long reviewId, @JsonProperty("author")String author, @JsonProperty("subject")String subject, @JsonProperty("content")String content) {
}
