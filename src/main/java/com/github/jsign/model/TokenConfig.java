package com.github.jsign.model;


public class TokenConfig {

	private Token token;
	private String libpath;
	private Integer slot;
	private OperatingSystem operatingSystem;
	
	public TokenConfig(Token token, OperatingSystem os, String libpath, Integer slot) {
		this(token, os, libpath);
		this.slot = slot;
	}

	public TokenConfig(Token token, OperatingSystem os, String libpath) {
		super();
		this.operatingSystem = os;
		this.token = token;
		this.libpath = libpath;
	}

	public Token getToken() {
		return token;
	}
	
	public void setToken(Token token) {
		this.token = token;
	}
	
	public String getLibpath() {
		return libpath;
	}
	
	public void setLibpath(String libpath) {
		this.libpath = libpath;
	}

	public Integer getSlot() {
		return slot;
	}

	public void setSlot(Integer slot) {
		this.slot = slot;
	}

	public OperatingSystem getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(OperatingSystem operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
}
