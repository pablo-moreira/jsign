package com.github.jsign.keystore;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.security.auth.callback.CallbackHandler;


import com.github.jsign.model.KeyStoreType;
import com.github.jsign.model.TokenConfig;


public class PKCS11KeyStoreHelper extends KeyStoreHelper {
	
	private TokenConfig tokenConfig;
	private CallbackHandler callbackHandler;
	private long slot;
	
	public PKCS11KeyStoreHelper(TokenConfig tokenConfig, long slot, CallbackHandler callbackHandler, KeyStore keyStore, X509Certificate certificate) {
		this.tokenConfig = tokenConfig;
		this.slot = slot;
		this.callbackHandler = callbackHandler;
		this.certificate = certificate;
		this.keyStore = keyStore;
	}

	@Override
	public String getType() {
		return KeyStoreType.PKCS11.name();
	}
}
