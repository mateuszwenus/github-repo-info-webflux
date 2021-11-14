package com.github.mateuszwenus.github_repo_info_webflux;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RepositoryInfoControllerEnd2EndTest {

	private static final String OWNER = "mateuszwenus";
	private static final String REPOSITORY_NAME = "github-repo-info-webflux";

	@Autowired
	private WebTestClient testClient;

	@Test
	public void shouldReturnHttp200ForExistingRepository() {
		testClient.get()
			.uri("/repositories/" + OWNER + "/" + REPOSITORY_NAME)
			.exchange()
			.expectStatus()
			.isOk();
	}

	@Test
	public void shouldReturnHttp404WhenOwnerDoesNotExist() {
		testClient.get()
			.uri("/repositories/" + OWNER + OWNER + "/" + REPOSITORY_NAME)
			.exchange()
			.expectStatus()
			.isNotFound();
	}

	@Test
	public void shouldReturnHttp404WhenRepositoryDoesNotExist() {
		testClient.get()
			.uri("/repositories/" + OWNER + "/" + REPOSITORY_NAME + REPOSITORY_NAME)
			.exchange()
			.expectStatus()
			.isNotFound();
	}

}
