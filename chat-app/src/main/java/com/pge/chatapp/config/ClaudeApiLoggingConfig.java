package com.pge.chatapp.config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.http.okhttp.AnthropicHttpClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaudeApiLoggingConfig {

	private static final Logger logger = LoggerFactory.getLogger(ClaudeApiLoggingConfig.class);

	// Logs the endpoint, status and duration of every HTTP call to the Claude API.
	// Headers are not logged, so the API key never reaches the log
	@Bean
	AnthropicHttpClientBuilderCustomizer claudeApiEndpointLogging() {
		return (builder) -> builder.interceptor((chain) -> {
			Request request = chain.request();
			logger.info("--> {} {}", request.method(), request.url());
			long start = System.nanoTime();
			try {
				Response response = chain.proceed(request);
				logger.info("<-- {} {} {} ({} ms)", response.code(), request.method(), request.url(), elapsedMillis(start));
				return response;
			}
			catch (IOException ex) {
				logger.warn("<-- FAILED {} {} ({} ms): {}", request.method(), request.url(), elapsedMillis(start),
						ex.toString());
				throw ex;
			}
		});
	}

	private static long elapsedMillis(long start) {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
	}

}
