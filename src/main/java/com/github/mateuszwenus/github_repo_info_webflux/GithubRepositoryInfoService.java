package com.github.mateuszwenus.github_repo_info_webflux;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class GithubRepositoryInfoService implements RepositoryInfoService {

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
				.map(RepositoryInfo::new);
	}
}
