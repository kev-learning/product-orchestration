package com.microservices.core.product.orchestration.service.dto;

import lombok.Builder;

@Builder
public record ServiceAddressesDTO(String orchestrationAddress, String productAddress, String reviewAddress, String recommendationAddress) {
}
