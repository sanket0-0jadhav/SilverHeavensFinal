package com.developer.silverheavens.exceptions;

public class NewRateValidationException extends RuntimeException{
	public NewRateValidationException(String message) {
		super("New Rate Validation failed. Reason : "+message);
	}
}
