package com.github.mateuszwenus.github_repo_info_webflux;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class GithubRepositoryInfoServiceTest {

	private static final String OWNER = "testOwner";
	private static final String REPOSITORY_NAME = "testRepo";

	private MockWebServer mockServer;
	private WebClient webClient;
	private GithubRepositoryInfoService svc;

	@BeforeEach
	public void init() {
		mockServer = new MockWebServer();
		webClient = WebClient.builder()
				.baseUrl(mockServer.url("/").toString())
				.build();
		svc = new GithubRepositoryInfoService(webClient);
	}

	@AfterEach
	public void cleanup() throws IOException {
		mockServer.shutdown();
	}

	@Test
	public void shouldSendRequestToCorrectUrl() throws InterruptedException {
		// given
		mockServer.enqueue(new MockResponse());
		// when
		svc.getRepositoryInfo(OWNER, REPOSITORY_NAME).block();
		// then
		assertThat(mockServer.takeRequest().getPath(), is("/repos/" + OWNER + "/" + REPOSITORY_NAME));
	}
	
	@Test
	public void shouldReturnValidResponse() throws JSONException {
		// given
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
		// when
		RepositoryInfo info = svc.getRepositoryInfo(OWNER, REPOSITORY_NAME).block();
		// then
		assertThat(info, notNullValue());
		assertThat(info.fullName(), is("Test repository"));
		assertThat(info.description(), is("Abc"));
		assertThat(info.cloneUrl(), is("http://clone"));
		assertThat(info.stars(), is(10));
		assertThat(info.createdAt(), is("today"));
	}

	@Test
	public void shouldThrowExceptionWhenUserIsNotFound() {
		assertThrows(RepositoryNotFoundException.class, () -> {
			// given
			mockServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));
			// when
			svc.getRepositoryInfo(OWNER, REPOSITORY_NAME).block();
		});
	}

	@Test
	public void shouldThrowExceptionForHttpErrorOtherThan404() {
		assertThrows(RepositoryInfoServiceException.class, () -> {
			// given
			mockServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));
			// when
			svc.getRepositoryInfo(OWNER, REPOSITORY_NAME).block();
		});
	}
}
