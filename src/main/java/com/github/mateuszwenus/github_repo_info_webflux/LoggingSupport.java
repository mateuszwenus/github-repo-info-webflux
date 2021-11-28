package com.github.mateuszwenus.github_repo_info_webflux;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static net.logstash.logback.argument.StructuredArguments.v;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.logstash.logback.argument.StructuredArgument;

public class LoggingSupport {

	private static final Logger logger = LoggerFactory.getLogger(LoggingSupport.class);

	public static <T> Consumer<T> logMonoSuccess(String operationName) {
		return it -> logOperation(OperationType.MONO, operationName, "SUCCESS");
	}

	public static Consumer<Throwable> logMonoError(String name) {
		return exc -> logOperation(OperationType.MONO, name, "ERROR", 
				v("excClass", exc.getClass().getCanonicalName()), v("excMessage", exc.getMessage()));
	}

	public static Runnable logMonoCancel(String operationName) {
		return () -> logOperation(OperationType.MONO, operationName, "CANCEL");
	}

	public static void logOperation(OperationType operationType, String operationName, Object status, StructuredArgument... arguments) {
		StructuredArgument[] loggerArguments = new StructuredArgument[3 + (arguments != null? arguments.length : 0)];
		loggerArguments[0] = v("operationType", operationType);
		loggerArguments[1] = kv("operation", operationName);
		loggerArguments[2] = kv("status", String.valueOf(status));
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				loggerArguments[3 + i] = arguments[i];
			}
		}
		logger.info("{} {} {}", (Object[]) loggerArguments);
	}

	static enum OperationType {
		API, HTTP, MONO
	}
}
