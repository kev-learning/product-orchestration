package com.microservices.core.product.orchestration.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ServiceAddressesDTO(@JsonProperty("orchestrationAddress")String orchestrationAddress, @JsonProperty("productAddress")String productAddress, @JsonProperty("reviewAddress")String reviewAddress, @JsonProperty("recommendationAddress")String recommendationAddress) {
}
