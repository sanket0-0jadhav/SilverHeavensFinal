package com.developer.silverheavens.exceptions;

public class UpdateException extends RuntimeException {
	public UpdateException(int id,String message) {
		super("Cannot Update {"+id+"} : "+message);
	}
}
