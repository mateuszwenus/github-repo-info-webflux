package com.github.mateuszwenus.github_repo_info_webflux;

import static com.github.mateuszwenus.github_repo_info_webflux.LoggingSupport.logMonoCancel;
import static com.github.mateuszwenus.github_repo_info_webflux.LoggingSupport.logMonoError;
import static com.github.mateuszwenus.github_repo_info_webflux.LoggingSupport.logMonoSuccess;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class GithubRepositoryInfoService implements RepositoryInfoService {

	private static final String GET_REPO_INFO = "getRepositoryInfo";
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
				.doOnSuccess(logMonoSuccess(GET_REPO_INFO))
				.doOnError(logMonoError(GET_REPO_INFO))
				.doOnCancel(logMonoCancel(GET_REPO_INFO));
	}
}
