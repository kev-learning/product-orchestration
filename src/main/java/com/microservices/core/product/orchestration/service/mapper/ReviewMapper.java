package com.microservices.core.product.orchestration.service.mapper;

import com.microservices.core.product.orchestration.service.dto.ReviewSummaryDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ReviewDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mappings({
            @Mapping(target = "productId", source = "productDTO.productId"),
            @Mapping(target = "serviceAddress", ignore = true)
    })
    ReviewDTO mapAtoB(ReviewSummaryDTO reviewSummaryDTOS, ProductDTO productDTO);

    @Mappings({})
    ReviewSummaryDTO mapBtoA(ReviewDTO reviewDTO);
}
