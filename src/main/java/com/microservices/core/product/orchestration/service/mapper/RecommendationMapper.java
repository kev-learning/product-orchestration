package com.microservices.core.product.orchestration.service.mapper;

import com.microservices.core.product.orchestration.service.dto.RecommendationSummaryDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.product.orchestration.service.remote.dto.RecommendationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true),
            @Mapping(target = "productId", source = "productDTO.productId")
    })
    RecommendationDTO mapAtoB(RecommendationSummaryDTO recommendationSummaryDTO, ProductDTO productDTO);

    @Mappings({})
    RecommendationSummaryDTO mapBtoA(RecommendationDTO recommendationDTO);
}