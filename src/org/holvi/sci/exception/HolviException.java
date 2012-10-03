package org.holvi.sci.exception;

public class HolviException extends Exception {
	private static final long serialVersionUID = -4804969046213104618L;
	private int id;
	private String message;
	private String type;
	
	public HolviException(int id, String message, String type) {
		this.setId(id);
		this.setMessage(message);
		this.setType(type);
	}
	
	public HolviException() {
		
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}

