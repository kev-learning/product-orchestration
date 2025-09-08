package com.microservices.core.product.orchestration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan("com.microservices.core")
public class ProductOrchestrationServiceApplication {

	@Value("${app.threadPoolSize:10}")
	Integer threadPoolSize;
	@Value("${app.taskQueueSize:100}")
	Integer taskQueueSize;

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public Scheduler publishEventScheduler() {
		return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
	}


	@Bean
	public WebClient loadBalancedWebClientBuilder(WebClient.Builder builder) {
		return builder.build();
	}

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(ProductOrchestrationServiceApplication.class, args);
	}

}
