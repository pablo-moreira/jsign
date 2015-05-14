package com.github.jsign.model;

import java.io.InputStream;

public class MessageToSign {

	private String name;
	
	private InputStream message;
	
	public MessageToSign(InputStream message) {
		super();
		this.message = message;
	}

	public MessageToSign(String name, InputStream message) {
		super();
		this.name = name;
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InputStream getMessage() {
		return message;
	}

	public void setMessage(InputStream message) {
		this.message = message;
	}

	public boolean isDefinedName() {
		return getName() != null;
	}	
}