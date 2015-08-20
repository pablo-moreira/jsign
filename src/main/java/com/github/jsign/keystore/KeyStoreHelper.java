package com.github.jsign.keystore;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.github.jsign.model.KeyStoreType;
import com.github.jsign.util.StringUtils;

public abstract class KeyStoreHelper {
	
	protected X509Certificate certificate;
	protected String certificateAlias;
	protected KeyStore keyStore;
	protected PrivateKey privateKey;
	protected Certificate[] certsChain;	

	public KeyStoreHelper(KeyStore keyStore, X509Certificate certificate) throws Exception {
		
		this.keyStore = keyStore;
		this.certificate = certificate;
					
		try {			
			this.certificateAlias = keyStore.getCertificateAlias(certificate);
		} 
		catch (KeyStoreException e) {
			throw new Exception("Não foi possível recuperar o alias do certificado, mensagem interna: " + e.getMessage());
		}
		
		if (StringUtils.isNullOrEmpty(this.certificateAlias)) {
			throw new Exception("Não existe o certificado " + certificate.getSubjectDN() + " no repositório!");
		}

		try {
			this.certsChain = keyStore.getCertificateChain(this.certificateAlias);
		} 
		catch (KeyStoreException e) {
			throw new Exception("Não foi possível recuperar a cadeia de certificado do certificado, mensagem interna: " + e.getMessage());
		}
	}
	
	public abstract KeyStoreType getType();
	public abstract String getDescription();

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
	
	public String getCertificateAlias() {
		return certificateAlias;
	}
		
	public static List<X509Certificate> getCertificatesAvailable(KeyStore keyStore) throws Exception {
		try {
			List<X509Certificate> certificates = new ArrayList<X509Certificate>();

			Enumeration<String> aliases = keyStore.aliases();

			while (aliases.hasMoreElements()) {
				
				String alias = (String) aliases.nextElement();

				if (keyStore.isKeyEntry(alias)) {
					certificates.add((X509Certificate) keyStore.getCertificate(alias));
				}
			}

			return certificates;
		}
		catch (KeyStoreException e) {
			throw new Exception("O KeyStore não foi inicializado corretamente!\n" + e);
		}
	}
}