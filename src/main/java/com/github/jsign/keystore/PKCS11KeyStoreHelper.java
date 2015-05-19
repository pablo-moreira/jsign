package com.github.jsign.keystore;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import com.github.jsign.model.KeyStoreType;
import com.github.jsign.model.TokenConfig;


public class PKCS11KeyStoreHelper extends KeyStoreHelper {
	
	private TokenConfig tokenConfig;
	private long slot;
	
	public PKCS11KeyStoreHelper(TokenConfig tokenConfig, long slot, KeyStore keyStore, X509Certificate certificate) {
		this.tokenConfig = tokenConfig;
		this.slot = slot;
		this.certificate = certificate;
		this.keyStore = keyStore;
	}

	@Override
	public KeyStoreType getType() {
		return KeyStoreType.PKCS11;
	}

	public TokenConfig getTokenConfig() {
		return tokenConfig;
	}

	public long getSlot() {
		return slot;
	}
}