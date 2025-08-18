package com.microservices.core.product.orchestration.service.remote.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ReviewDTO(@JsonProperty("reviewId")Long reviewId, @JsonProperty("productId")Long productId, @JsonProperty("author")String author, @JsonProperty("subject")String subject, @JsonProperty("content")String content, @JsonProperty("serviceAddress")String serviceAddress) {

}
