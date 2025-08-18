package com.microservices.core.product.orchestration.service.mapper;

import com.microservices.core.product.orchestration.service.dto.ReviewSummaryDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ProductDTO;
import com.microservices.core.product.orchestration.service.remote.dto.ReviewDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ReviewMapperTest {

    private static final Long COMMON_ID = 1L;

    ReviewMapper reviewMapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    void mapAtoBTest() {
        ReviewSummaryDTO reviewSummaryDTO = ReviewSummaryDTO.builder()
                .reviewId(COMMON_ID)
                .author("AUTHOR")
                .subject("SUBJECT")
                .content("CONTENT")
                .build();

        ReviewDTO reviewDTO = reviewMapper.mapAtoB(reviewSummaryDTO, buildProduct());

        assertEquals(reviewSummaryDTO.reviewId(), reviewDTO.reviewId());
        assertEquals(reviewSummaryDTO.author(), reviewDTO.author());
        assertEquals(reviewSummaryDTO.subject(), reviewDTO.subject());
        assertEquals(reviewSummaryDTO.content(), reviewDTO.content());
    }

    @Test
    void mapBtoATest() {
        ReviewDTO reviewDTO = ReviewDTO.builder()
                .reviewId(COMMON_ID)
                .author("AUTHOR")
                .subject("SUBJECT")
                .content("CONTENT")
                .productId(COMMON_ID)
                .build();

        ReviewSummaryDTO reviewSummaryDTO = reviewMapper.mapBtoA(reviewDTO);

        assertEquals(reviewDTO.reviewId(), reviewSummaryDTO.reviewId());
        assertEquals(reviewDTO.author(), reviewSummaryDTO.author());
        assertEquals(reviewDTO.subject(), reviewSummaryDTO.subject());
        assertEquals(reviewDTO.content(), reviewSummaryDTO.content());
    }

    private ProductDTO buildProduct() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(COMMON_ID);
        productDTO.setName("NAME");
        productDTO.setWeight(COMMON_ID.intValue());

        return productDTO;
    }
}