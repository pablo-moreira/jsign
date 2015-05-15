package com.github.jsign.model;

import java.security.KeyStore;

public class PKCS11AvailableKeyStore {

	private TokenConfig tokenConfig;	
	private KeyStore keyStore;
	private long slot;
	
	public PKCS11AvailableKeyStore(TokenConfig tokenConfig, long slot, KeyStore keyStore) {
		super();
		this.tokenConfig = tokenConfig;
		this.slot = slot;
		this.keyStore = keyStore;
	}

	public TokenConfig getTokenConfig() {
		return tokenConfig;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public long getSlot() {
		return slot;
	}
}
