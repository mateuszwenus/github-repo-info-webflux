package com.github.mateuszwenus.github_repo_info_webflux;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static net.logstash.logback.argument.StructuredArguments.v;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@SpringBootApplication
public class Application {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${github.api.url}")
	private String githubApiUrl;
	@Value("${github.api.timeoutMillis}")
	private int timeoutMillis;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public WebClient webClient() {
		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMillis)
			.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(timeoutMillis, TimeUnit.MILLISECONDS)));

		return WebClient.builder()
				.baseUrl(githubApiUrl)
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
				.filters(exchangeFilterFunctions -> {
					exchangeFilterFunctions.add(logRequest());
					exchangeFilterFunctions.add(logResponse());
				})
				.build();
	}

	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(req -> {
			logger.info("request {} {} {}", kv("logPrefix", req.logPrefix()), v("method", req.method()), v("url", req.url()));
			return Mono.just(req);
		});
	}

	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(resp -> {
			logger.info("response {} {}", kv("logPrefix", resp.logPrefix()), kv("status", resp.statusCode()));
			return Mono.just(resp);
		});
	}
}
