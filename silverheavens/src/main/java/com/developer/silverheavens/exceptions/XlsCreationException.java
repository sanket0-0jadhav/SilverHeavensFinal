package com.developer.silverheavens.exceptions;

public class XlsCreationException extends RuntimeException {
	public XlsCreationException(String message) {
		super("Cannot export data : "+message);
	}
}
