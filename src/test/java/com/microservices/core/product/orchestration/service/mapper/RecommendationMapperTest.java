package com.microservices.core.product.orchestration.service.mapper;

import com.microservices.core.product.orchestration.service.dto.RecommendationSummaryDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.product.orchestration.service.remote.dto.RecommendationDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationMapperTest {

    private static final Long COMMON_ID = 1L;

    RecommendationMapper recommendationMapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapAToBTest() {
        RecommendationSummaryDTO recommendationSummaryDTO = RecommendationSummaryDTO.builder()
                .recommendationId(COMMON_ID)
                .author("AUTHOR")
                .content("CONTENT")
                .rating(COMMON_ID.intValue())
                .build();

        RecommendationDTO recommendationDTO = recommendationMapper.mapAtoB(recommendationSummaryDTO, buildProduct());

        assertEquals(recommendationSummaryDTO.recommendationId(), recommendationDTO.recommendationId());
        assertEquals(recommendationSummaryDTO.content(), recommendationDTO.content());
        assertEquals(recommendationSummaryDTO.author(), recommendationDTO.author());
        assertEquals(recommendationSummaryDTO.rating(), recommendationDTO.rating());
    }

    @Test
    void mapBtoATest() {
        RecommendationDTO recommendationDTO = RecommendationDTO.builder()
                .recommendationId(COMMON_ID)
                .productId(COMMON_ID)
                .rating(COMMON_ID.intValue())
                .author("AUTHOR")
                .content("CONTENT")
                .build();

        RecommendationSummaryDTO recommendationSummaryDTO = recommendationMapper.mapBtoA(recommendationDTO);

        assertEquals(recommendationDTO.recommendationId(), recommendationSummaryDTO.recommendationId());
        assertEquals(recommendationDTO.rating(), recommendationSummaryDTO.rating());
        assertEquals(recommendationDTO.author(), recommendationSummaryDTO.author());
        assertEquals(recommendationDTO.content(), recommendationSummaryDTO.content());
    }

    private ProductDTO buildProduct() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(COMMON_ID);
        productDTO.setName("NAME");
        productDTO.setWeight(COMMON_ID.intValue());

        return productDTO;
    }

}