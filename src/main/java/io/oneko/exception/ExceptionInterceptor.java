package io.oneko.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.oneko.helm.HelmRegistryException;

@ControllerAdvice
public class ExceptionInterceptor {
	@ExceptionHandler({HelmRegistryException.class, IllegalArgumentException.class})
	public ResponseEntity<?> badRequest(HelmRegistryException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}
}