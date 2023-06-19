package com.developer.silverheavens.exceptions;

public class IdNotFoundException extends RuntimeException {

	public IdNotFoundException(Class<?> entityClass,int id) {
		super("ID {"+id+"} NOT FOUND FOR : "+entityClass.getSimpleName());
	}
	
}
