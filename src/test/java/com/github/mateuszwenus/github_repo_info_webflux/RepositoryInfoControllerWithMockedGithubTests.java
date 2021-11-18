package com.github.mateuszwenus.github_repo_info_webflux;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("MOCKED-GITHUB")
public class RepositoryInfoControllerWithMockedGithubTests {

	@Value("${github.api.port}")
	private int githubApiPort;
	
	@Autowired
	private WebTestClient testClient;

	private MockWebServer mockServer;

	@BeforeEach
	public void init() throws IOException {
		mockServer = new MockWebServer();
		mockServer.start(githubApiPort);
	}

	@AfterEach
	public void cleanup() throws IOException {
		mockServer.shutdown();
	}

	@Test
	public void shouldReturnValidResponse() throws JSONException {
		String responseBody = new JSONObject()
				.put("full_name", "Test repository")
				.put("description", "Abc")
				.put("clone_url", "http://clone")
				.put("stargazers_count", 10)
				.put("created_at", "today")
				.toString();
		MockResponse response = new MockResponse()
				.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.setBody(responseBody);
		mockServer.enqueue(response);

		testClient.get()
			.uri("/repositories/owner/repo")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(RepositoryInfo.class)
			.isEqualTo(new RepositoryInfo("Test repository", "Abc", "http://clone", 10, "today"));
	}

	@Test
	public void shouldReturnHttp404WhenGithubReturnsHttp404() {
		mockServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));

		testClient.get()
			.uri("/repositories/owner/repo")
			.exchange()
			.expectStatus()
			.isNotFound();
	}

	@Test
	public void shouldReturnHttp500WhenGithubReturnsErrorOtherThan404() {
		mockServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));

		testClient.get()
			.uri("/repositories/owner/repo")
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
