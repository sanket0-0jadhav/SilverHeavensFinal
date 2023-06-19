package com.developer.silverheavens.exceptions;

public class XlsDataParsingException extends RuntimeException {
	public XlsDataParsingException(String cellAddress,String expected,String received) {
		super(" Date parsing exception at :"+cellAddress+". Expected "+expected+" got "+received);
	}
}
