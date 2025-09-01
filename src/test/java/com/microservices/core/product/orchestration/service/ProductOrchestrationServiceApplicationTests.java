package com.microservices.core.product.orchestration.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestSecurityConfig.class, properties = {"eureka.client.enabled=false", "spring.main.allow-bean-definition-overriding=true"})
@ImportAutoConfiguration(exclude = KafkaAutoConfiguration.class)
class ProductOrchestrationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
