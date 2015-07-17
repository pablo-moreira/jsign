package com.github.jsign.model;


public class MessageToSign {

	private String name;
	private byte[] message;
	
	public MessageToSign(byte[] message) {
		this.message = message;
	}

	public MessageToSign(String name, byte[] message) {
		this.name = name;
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

	public boolean isDefinedName() {
		return getName() != null;
	}	
}