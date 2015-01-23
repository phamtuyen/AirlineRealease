package com.mbv.ticketsystem.airline.jetstar;

@SuppressWarnings("serial")
public class JetstarException extends Exception {
	private Error error;

	public JetstarException(Error error) {
		super();
		this.error = error;		
	}
	
	public JetstarException(Error error, Exception exception) {
		super(exception);
		this.error = error;		
	}

	public Error getError() {
		return error;
	}
	
	public enum Error {
		CONNECTION_ERROR, LOGIN_ERROR, INVALID_REQUEST, INVALID_RESPONSE, OTHERS;
	}
}