package com.github.jsign.model;

import java.security.Provider;

public abstract class AvailableProvider {

	protected Provider provider;

	public AvailableProvider(Provider provider) {
		this.provider = provider;
	}

	public Provider getProvider() {
		return provider;
	}

	abstract public String getType();

	abstract public String getDescription();

}
