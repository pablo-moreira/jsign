package com.github.jsign.model;

import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.List;

public class MSCAPIAvailableProvider extends AvailableProvider {

	private KeyStore keyStore;
	private List<X509Certificate> certificates;

	public MSCAPIAvailableProvider(Provider provider, KeyStore keyStore, List<X509Certificate> certificates) {
		super(provider);
		this.keyStore = keyStore;
		this.certificates = certificates;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	@Override
	public String getType() {
		return Configuration.KEY_STORE_TYPE_MSCAPI;
	}

	@Override
	public String getDescription() {
		return "KeyStore: " + getKeyStore().getType() + ", Quant. Certificados: " + certificates.size(); 
	}	
}
