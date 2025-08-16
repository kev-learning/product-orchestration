package microservices.core.product.orchestration.service.remote;

import lombok.extern.slf4j.Slf4j;
import microservices.core.product.orchestration.service.dto.RecommendationSummaryDTO;
import microservices.core.product.orchestration.service.remote.dto.RecommendationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RecommendationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.service.recommendation.host}")
    private String recommendationServiceHost;

    @Value("${app.service.recommendation.port}")
    private Integer recommendationServicePort;

    public List<RecommendationSummaryDTO> buildRecommendationSummaries(List<RecommendationDTO> recommendations) {
        return Optional.ofNullable(recommendations).orElse(Collections.emptyList())
                .stream().map(recommendation -> RecommendationSummaryDTO.builder()
                        .recommendationId(recommendation.recommendationId())
                        .author(recommendation.author())
                        .rating(recommendation.rating())
                        .build()).toList();
    }

    public List<RecommendationDTO> getProductRecommendations(Long productId) {
        log.debug("Retrieving product recommendation for product ID: {}", productId);
        log.debug("URL: {}{}", getRecommendationServiceUrl(),productId);

        try {
            List<RecommendationDTO> recommendations = restTemplate.exchange(getRecommendationServiceUrl() + productId, HttpMethod.GET, null, new ParameterizedTypeReference<List<RecommendationDTO>>() {}).getBody();

            log.debug("Found recommendations: {}", recommendations);
            return recommendations;
        }catch(HttpClientErrorException ex) {
            log.warn("Got an error during recommendations retrieval: HTTP Status:{}, message: {}", ex.getMessage(), ex.getResponseBodyAsString());
            return Collections.emptyList();
        }
    }

    private String getRecommendationServiceUrl() {
        return "http://%s:%s/recommendation?productId=".formatted(recommendationServiceHost, recommendationServicePort);
    }
}
