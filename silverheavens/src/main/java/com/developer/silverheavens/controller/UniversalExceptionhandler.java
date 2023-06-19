package com.developer.silverheavens.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.developer.silverheavens.dto.ResponseDto;
import com.developer.silverheavens.dto.ResponseStatus;
import com.developer.silverheavens.exceptions.IdNotFoundException;

@ControllerAdvice
public class UniversalExceptionhandler {
	//TESTING
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ResponseDto<String>> exception(NullPointerException ex){
		
		ResponseDto<String> resp = new ResponseDto<String>(ResponseStatus.FAIL, null,"TEST EXCEPTION");
		return new ResponseEntity<ResponseDto<String>>(resp,HttpStatus.BAD_REQUEST);
	}
	
	//ID NOT PRESENT
	@ExceptionHandler(IdNotFoundException.class)
	public ResponseEntity<ResponseDto<String>> exception(IdNotFoundException ex){
		
		ResponseDto<String> resp = new ResponseDto<String>(ResponseStatus.FAIL, null,ex.getMessage());
		return new ResponseEntity<ResponseDto<String>>(resp,HttpStatus.BAD_REQUEST);
	}
	
	//UNCAUGHT EXCEPTION
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseDto<String>> exception(Exception ex){
		
		ResponseDto<String> resp = new ResponseDto<String>(ResponseStatus.FAIL, null,ex.getMessage());
		return new ResponseEntity<ResponseDto<String>>(resp,HttpStatus.BAD_REQUEST);
	}
	
}
