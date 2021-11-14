package com.github.mateuszwenus.github_repo_info_webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public WebClient webClient() {
		return WebClient.builder()
				.baseUrl("https://api.github.com")
				.defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
				.build();
	}
}
