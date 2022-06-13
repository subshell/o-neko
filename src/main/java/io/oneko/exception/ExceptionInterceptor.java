package io.oneko.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.oneko.helm.HelmRegistryException;

@ControllerAdvice
public class ExceptionInterceptor {

	@ExceptionHandler(value = RuntimeException.class)
	public ResponseEntity<?> badRequest(RuntimeException e) {
		if (e.getCause() instanceof HelmRegistryException) {
			return badRequestForHelmRegistry((HelmRegistryException) e.getCause());
		}
		return new ResponseEntity<>("An unexpected error occurred while handling the request", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = IllegalArgumentException.class)
	public ResponseEntity<?> badRequest(IllegalArgumentException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = HelmRegistryException.class)
	public ResponseEntity<?> badRequestForHelmRegistry(HelmRegistryException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}


}