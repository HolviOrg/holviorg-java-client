package org.holvi.sci.exception;

public class HolviEncryptionException extends Exception {
	private static final long serialVersionUID = 1472228041803856452L;
	private int id;
	private String message;
	
	public HolviEncryptionException(int id, String message) {
		this.setMessage(message);
		this.setId(id);
	}
	
	public HolviEncryptionException() {
		
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
