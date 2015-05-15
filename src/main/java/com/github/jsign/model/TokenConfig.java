package com.github.jsign.model;


public class TokenConfig {

	private Token token;
	private String library;
	private OperatingSystem operatingSystem;
	
	public TokenConfig(Token token, OperatingSystem operatingSystem, String library) {
		super();
		this.operatingSystem = operatingSystem;
		this.token = token;
		this.library = library;
	}

	public Token getToken() {
		return token;
	}
	
	public void setToken(Token token) {
		this.token = token;
	}
	
	public String getLibrary() {
		return library;
	}
	
	public void setLibrary(String library) {
		this.library = library;
	}

	public OperatingSystem getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(OperatingSystem operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
}
