package com.microservices.core.product.orchestration.service.mapper;

import com.microservices.core.product.orchestration.service.dto.ProductAggregateDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private static final Long COMMON_ID = 1L;

    ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapAtoBTest() {
        ProductAggregateDTO productAggregateDTO = ProductAggregateDTO.builder()
                .productId(COMMON_ID)
                .name("NAME")
                .weight(COMMON_ID.intValue())
                .build();

        ProductDTO productDTO = productMapper.mapAtoB(productAggregateDTO);

        assertEquals(productAggregateDTO.productId(), productDTO.getProductId());
        assertEquals(productAggregateDTO.name(), productDTO.getName());
        assertEquals(productAggregateDTO.weight(), productDTO.getWeight());
        assertNull(productAggregateDTO.recommendationSummaries());
        assertNull(productAggregateDTO.reviewSummaries());
        assertNull(productAggregateDTO.serviceAddresses());
        assertNull(productAggregateDTO.warnings());
    }

    @Test
    void mapBtoATest() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(COMMON_ID);
        productDTO.setName("NAME");
        productDTO.setWeight(COMMON_ID.intValue());

        ProductAggregateDTO productAggregateDTO = productMapper.mapBtoA(productDTO);

        assertEquals(productDTO.getProductId(), productAggregateDTO.productId());
        assertEquals(productDTO.getName(), productAggregateDTO.name());
        assertEquals(productDTO.getWeight(), productAggregateDTO.weight());
        assertNull(productDTO.getServiceAddress());
    }
}