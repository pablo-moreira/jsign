package com.github.jsign.keystore;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import com.github.jsign.model.KeyStoreType;

public class MSCAPIKeyStoreHelper extends KeyStoreHelper {
			
	public MSCAPIKeyStoreHelper(KeyStore keyStore, X509Certificate certificate) throws Exception  {
		
		super(keyStore, certificate);
		
		try {	
			this.privateKey = (PrivateKey) keyStore.getKey(this.certificateAlias, null);
		}
		catch (Exception e) {
			throw new Exception("Erro ao obter certificado!\n" + e);
		}
	}

	@Override
	public KeyStoreType getType() {
		return KeyStoreType.MSCAPI;
	}

	@Override
	public String getDescription() {
		return getCertificateAlias();
	}
}
