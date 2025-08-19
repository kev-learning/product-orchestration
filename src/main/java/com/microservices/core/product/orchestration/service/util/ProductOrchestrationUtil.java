package com.microservices.core.product.orchestration.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.core.util.exceptions.InvalidInputException;
import com.microservices.core.util.exceptions.NotFoundException;
import com.microservices.core.util.http.HttpErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class ProductOrchestrationUtil {

    private static final String WARNING_MSG = "Unexpected error: {} with response body: {}";

    private ProductOrchestrationUtil() {

    }

    public static String getErrorMessage(ObjectMapper objectMapper, WebClientResponseException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }catch (IOException ioe) {
            return ex.getMessage();
        }
    }

    public static Throwable handleWebClientException(WebClientResponseException ex, ObjectMapper objectMapper) {
        switch (Optional.ofNullable(HttpStatus.resolve(ex.getStatusCode().value())).orElse(HttpStatus.INTERNAL_SERVER_ERROR)) {
            case NOT_FOUND -> throw new NotFoundException(ProductOrchestrationUtil.getErrorMessage(objectMapper, ex));
            case BAD_REQUEST -> throw new InvalidInputException(ProductOrchestrationUtil.getErrorMessage(objectMapper, ex));
            default -> {
                log.warn(WARNING_MSG, ex.getStatusCode(), ex.getResponseBodyAsString());
                throw ex;
            }
        }
    }
}
