package com.github.mateuszwenus.github_repo_info_webflux;

import static com.github.mateuszwenus.github_repo_info_webflux.LoggingSupport.logOperation;
import static net.logstash.logback.argument.StructuredArguments.kv;
import static net.logstash.logback.argument.StructuredArguments.v;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;

import com.github.mateuszwenus.github_repo_info_webflux.LoggingSupport.OperationType;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
public class ApplicationConfig {

	@Value("${github.api.url}")
	private String githubApiUrl;
	@Value("${github.api.timeoutMillis}")
	private int timeoutMillis;

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
			String operationName = req.logPrefix() + " " + req.method() + " " + req.url();
			logOperation(OperationType.HTTP, operationName, "<unknown>", 
					kv("logPrefix", req.logPrefix()), v("method", req.method()), v("url", req.url()));
			return Mono.just(req);
		});
	}

	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(resp -> {
			String operationName = resp.logPrefix() + " response";
			logOperation(OperationType.HTTP, operationName, resp.statusCode(), v("logPrefix", resp.logPrefix()));
			return Mono.just(resp);
		});
	}
	
	@Bean
	public WebFilter loggingFilter() {
		return (exchange, filterChain) -> {
			return filterChain.filter(exchange).doFinally(signal -> {
				ServerHttpRequest req = exchange.getRequest();
				ServerHttpResponse resp = exchange.getResponse();
				String operationName = req.getMethod() + " " + req.getPath();
				logOperation(OperationType.API, operationName, resp.getStatusCode(), v("method", req.getMethod()), v("path", req.getPath()));
			});
		};
	}

}
