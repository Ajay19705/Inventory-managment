package com.Invnmgmt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.Invnmgmt.dtos.Response;

@RestControllerAdvice
public class GlobalExceptionHandler {

	 @ExceptionHandler(Exception.class)
	    @ResponseBody
	    public ResponseEntity<Response> handleAllExceptions(Exception ex) {
	        Response response = new Response();
	        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
	        response.setMessage(ex.getMessage());  // ✅ FIX: include message
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    @ExceptionHandler(NotFoundException.class)
	    @ResponseBody
	    public ResponseEntity<Response> handleNotFoundException(NotFoundException ex) {
	        Response response = new Response();
	        response.setStatus(HttpStatus.NOT_FOUND.value());
	        response.setMessage(ex.getMessage());  // ✅ FIX: include message
	        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	    }

	    @ExceptionHandler(NameValueRequiredException.class)
	    @ResponseBody
	    public ResponseEntity<Response> handleNameValueRequiredException(NameValueRequiredException ex) {
	        Response response = new Response();
	        response.setStatus(HttpStatus.BAD_REQUEST.value());
	        response.setMessage(ex.getMessage());  // ✅ FIX: include message
	        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	    }

	    @ExceptionHandler(InvalidCredentialsException.class)
	    @ResponseBody
	    public ResponseEntity<Response> handleInvalidCredentialsException(InvalidCredentialsException ex) {
	        Response response = new Response();
	        response.setStatus(HttpStatus.UNAUTHORIZED.value());
	        response.setMessage(ex.getMessage());  // ✅ FIX: include message
	        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	    }    
}
