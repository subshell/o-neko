package io.oneko.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionInterceptor {

	@ExceptionHandler(value = IllegalArgumentException.class)
	public ResponseEntity badRequest() {
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

}