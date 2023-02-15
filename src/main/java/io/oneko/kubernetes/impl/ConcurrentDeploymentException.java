package io.oneko.kubernetes.impl;

public class ConcurrentDeploymentException extends RuntimeException {
	public ConcurrentDeploymentException() {
	}

	public ConcurrentDeploymentException(String message) {
		super(message);
	}

	public ConcurrentDeploymentException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConcurrentDeploymentException(Throwable cause) {
		super(cause);
	}

	public ConcurrentDeploymentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
