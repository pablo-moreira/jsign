package com.github.jsign.model;

import java.util.ArrayList;
import java.util.List;

public class Token {
	
	private String name;
	private List<TokenConfig> configs = new ArrayList<TokenConfig>();

	public Token(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TokenConfig> getConfigs() {
		return configs;
	}

	public void setConfigs(List<TokenConfig> configs) {
		this.configs = configs;
	}

	private Token addLib(OperatingSystem operatingSystem, String library) {
		
		getConfigs().add(new TokenConfig(this, operatingSystem, library));
		
		return this;
	}
	
	private Token addLib(OperatingSystem operatingSystem, String library, Integer slot) {
	
		getConfigs().add(new TokenConfig(this, operatingSystem, library, slot));
		
		return this;
	}

	public Token addLibLinux(String library) {
		return addLib(OperatingSystem.LINUX, library);
	}
	
	public Token addLibWindows(String library) {
		return addLib(OperatingSystem.WINDOWS, library);
	}
	
	public Token addLibWindows(String library, Integer slot) {
		return addLib(OperatingSystem.WINDOWS, library, slot);
	}

	public Token addLibMacOS(String libpath) {
		return addLib(OperatingSystem.MACOS, libpath);
	}

	public List<TokenConfig> getConfigsBySO(OperatingSystem operatingSystem) {

		List<TokenConfig> configs = new ArrayList<TokenConfig>();

		for (TokenConfig tokenConfig : getConfigs()) {
			if (tokenConfig.getOperatingSystem() == operatingSystem) {
				configs.add(tokenConfig);
			}
		}
		
		return configs;
	}	
}
