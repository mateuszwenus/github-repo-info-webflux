package com.github.mateuszwenus.github_repo_info_webflux;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static net.logstash.logback.argument.StructuredArguments.v;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class GithubRepositoryInfoService implements RepositoryInfoService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final WebClient webClient;

	public GithubRepositoryInfoService(WebClient webClient) {
		this.webClient = webClient;
	}

	@Override
	public Mono<RepositoryInfo> getRepositoryInfo(String owner, String repositoryName) {
		return webClient.get()
				.uri("/repos/" + owner + "/" + repositoryName)
				.retrieve()
				.onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.error(new RepositoryNotFoundException()))
				.onStatus(HttpStatus::isError, clientResponse -> Mono.error(new RepositoryInfoServiceException()))
				.bodyToMono(GithubResponse.class)
				.map(RepositoryInfo::new)
				.doOnSuccess(logSuccess("getRepositoryInfo"))
				.doOnError(logError("getRepositoryInfo"))
				.doOnCancel(logCancel("getRepositoryInfo"));
	}

	private <T> Consumer<T> logSuccess(String name) {
		return it -> logger.info("{} ended with {}", kv("operation", name), kv("status", "success"));
	}

	private Consumer<Throwable> logError(String name) {
		return exc -> logger.info("{} ended with {}: ({}: {})", kv("operation", name), kv("status", "error"),
				v("excClass", exc.getClass().getCanonicalName()), v("excMessage", exc.getMessage()));
	}

	private Runnable logCancel(String name) {
		return () -> logger.info("{} ended with {}", kv("operation", name), kv("status", "cancel"));
	}
}
