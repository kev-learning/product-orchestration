package microservices.core.product.orchestration.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.core.util.http.HttpErrorInfo;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

public class ProductOrchestrationUtil {

    private ProductOrchestrationUtil() {

    }

    public static String getErrorMessage(ObjectMapper objectMapper, HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }catch (IOException ioe) {
            return ex.getMessage();
        }
    }
}
