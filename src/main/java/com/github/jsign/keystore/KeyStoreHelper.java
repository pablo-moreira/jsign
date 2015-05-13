package com.github.jsign.keystore;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public abstract class KeyStoreHelper {
	
	protected X509Certificate certificate;
	protected KeyStore keyStore;
	protected PrivateKey privateKey;
	protected Certificate[] certsChain;	

	public Certificate[] getCertsChain() {
		return certsChain;
	}

	public X509Certificate getCertificate() {
		return certificate;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
}
