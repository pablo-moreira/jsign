package com.github.jsign.model;

import java.util.ArrayList;
import java.util.List;

public class Token {
	
	private String name;
	private List<TokenConfig> libs = new ArrayList<TokenConfig>();

	public Token(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TokenConfig> getLibs() {
		return libs;
	}

	public void setLibs(List<TokenConfig> libs) {
		this.libs = libs;
	}

	private Token addLib(OperatingSystem os, String libpath) {
		
		getLibs().add(new TokenConfig(this, os, libpath));
		
		return this;
	}
	
	private Token addLib(OperatingSystem os, String libpath, Integer slot) {
	
		getLibs().add(new TokenConfig(this, os, libpath, slot));
		
		return this;
	}

	public Token addLibLinux(String libpath) {
		return addLib(OperatingSystem.LINUX, libpath);
	}
	
	public Token addLibWindows(String libpath) {
		return addLib(OperatingSystem.WINDOWS, libpath);
	}
	
	public Token addLibWindows(String libpath, Integer slot) {
		return addLib(OperatingSystem.WINDOWS, libpath, slot);
	}

	public Token addLibMacOS(String libpath) {
		return addLib(OperatingSystem.MACOS, libpath);
	}	
}
