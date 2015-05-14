package com.github.jsign.model;

import java.security.Provider;

public class PKCS11AvailableProvider {

	private Provider provider;
	private TokenConfig tokenConfig;

	public PKCS11AvailableProvider(Provider provider, TokenConfig tokenConfig) {
		super();
		this.provider = provider;
		this.tokenConfig = tokenConfig;
	}

	public Provider getProvider() {
		return provider;
	}

	public TokenConfig getTokenConfig() {
		return tokenConfig;
	}
}
