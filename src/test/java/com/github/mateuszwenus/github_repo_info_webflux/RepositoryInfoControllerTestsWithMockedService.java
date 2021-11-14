package com.github.mateuszwenus.github_repo_info_webflux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@WebFluxTest
public class RepositoryInfoControllerTestsWithMockedService {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private RepositoryInfoService repositoryInfoService;

	@Test
	public void shouldReturnHttp200WhenServiceReturnsValidResponse() throws Exception {
		RepositoryInfo repoInfo = new RepositoryInfo("Mock repository", "description", "http://clone", 0, "now");
		when(repositoryInfoService.getRepositoryInfo(any(), any())).thenReturn(Mono.just(repoInfo));

		webTestClient.get()
			.uri("/repositories/owner/repo")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(RepositoryInfo.class)
			.isEqualTo(repoInfo);
	}

	@Test
	public void shouldReturnHttp404WhenServiceReturnsNotFoundException() throws Exception {
		when(repositoryInfoService.getRepositoryInfo(any(), any())).thenReturn(Mono.error(new RepositoryNotFoundException()));

		webTestClient.get()
			.uri("/repositories/owner/repo")
			.exchange()
			.expectStatus()
			.isNotFound();
	}

	@Test
	public void shouldReturnHttp5xxWhenServiceReturnsGenericException() throws Exception {
		when(repositoryInfoService.getRepositoryInfo(any(), any())).thenReturn(Mono.error(new RepositoryInfoServiceException()));
		
		webTestClient.get()
			.uri("/repositories/owner/repo")
			.exchange()
			.expectStatus()
			.is5xxServerError();
	}
}
