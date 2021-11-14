package com.github.mateuszwenus.github_repo_info_webflux;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

@RestController
public class RepositoryInfoController {

	private final RepositoryInfoService repositoryInfoService;

	public RepositoryInfoController(RepositoryInfoService repositoryInfoService) {
		this.repositoryInfoService = repositoryInfoService;
	}

	@GetMapping("/repositories/{owner}/{repositoryName}")
	public Mono<ResponseEntity<RepositoryInfo>> repositoryInfo(@PathVariable String owner,
			@PathVariable String repositoryName) {
		return repositoryInfoService.getRepositoryInfo(owner, repositoryName).map(ResponseEntity::ok)
				.onErrorMap(RepositoryNotFoundException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"))
				.onErrorMap(RepositoryInfoServiceException.class, e -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong"));
	}
}
