package com.microservices.core.product.orchestration.service.mapper;

import com.microservices.core.product.orchestration.service.dto.ProductAggregateDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    ProductDTO mapAtoB(ProductAggregateDTO productAggregateDTO);

    @Mappings({
            @Mapping(target = "recommendationSummaries", ignore = true),
            @Mapping(target = "reviewSummaries", ignore = true),
            @Mapping(target = "serviceAddresses", ignore = true),
            @Mapping(target = "warnings", ignore = true)
    })
    ProductAggregateDTO mapBtoA(ProductDTO productDTO);
}
