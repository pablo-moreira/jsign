package com.github.jsign.model;

import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.List;

public class MSCAPIAvailableProvider extends AvailableProvider {

	private KeyStore keyStore;
	private List<X509Certificate> certificates;
	private Provider provider;

	public MSCAPIAvailableProvider(Provider provider, KeyStore keyStore, List<X509Certificate> certificates) {
		this.provider = provider;
		this.keyStore = keyStore;
		this.certificates = certificates;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	@Override
	public KeyStoreType getType() {
		return KeyStoreType.MSCAPI;
	}

	public Provider getProvider() {
		return provider;
	}
	
	@Override
	public String getDescription() {
		return "KeyStore: " + getKeyStore().getType() + ", Quant. Certificados: " + certificates.size(); 
	}

	public List<X509Certificate> getCertificates() {
		return certificates;
	}
}
