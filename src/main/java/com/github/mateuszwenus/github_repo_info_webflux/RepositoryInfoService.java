package com.github.mateuszwenus.github_repo_info_webflux;

import reactor.core.publisher.Mono;

public interface RepositoryInfoService {

	Mono<RepositoryInfo> getRepositoryInfo(String owner, String repositoryName);

}
