package com.github.jsign.model;

import java.security.KeyStore;
import java.security.Provider;

public class PKCS11AvailableProvider extends AvailableProvider {

	private Long slot;
	private TokenConfig tokenConfig;
	private KeyStore keyStore;
	private Provider provider;

	public PKCS11AvailableProvider(Provider provider, TokenConfig tokenConfig, Long slot) {
		this.provider = provider;
		this.tokenConfig = tokenConfig;
		this.slot = slot;
	}

	public PKCS11AvailableProvider(Provider provider, TokenConfig tokenConfig, Long slot, KeyStore keyStore) {
		this(provider, tokenConfig, slot); 
		this.keyStore = keyStore;
	}

	public Long getSlot() {
		return slot;
	}

	public TokenConfig getTokenConfig() {
		return tokenConfig;
	}

	@Override
	public KeyStoreType getType() {
		return KeyStoreType.PKCS11;
	}

	@Override
	public String getDescription() {
		
		String description = "";

		if (tokenConfig != null) {
			description += "Name: " + tokenConfig.getToken().getName() + ", Library: " + tokenConfig.getLibrary();
		}
		
		if (provider != null) {
			description += ", Provider: " + provider;
		}
		
		if (slot != null) {
			description += ", Slot: " + slot;
		}
		
		return description;
	}

	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;		
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public Provider getProvider() {
		return provider;
	}
}