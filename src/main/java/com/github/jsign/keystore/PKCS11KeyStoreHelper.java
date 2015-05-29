package com.github.jsign.keystore;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import com.github.jsign.model.KeyStoreType;
import com.github.jsign.model.TokenConfig;


public class PKCS11KeyStoreHelper extends KeyStoreHelper {
	
	private TokenConfig tokenConfig;
	private long slot;
	
	public PKCS11KeyStoreHelper(TokenConfig tokenConfig, long slot, KeyStore keyStore, X509Certificate certificate) throws Exception {
		
		try {
			this.certificateAlias = keyStore.getCertificateAlias(certificate);
		}
		catch (KeyStoreException e) {
			throw new Exception("Não foi possível recuperar o alias do certificado PKCS11, mensagem interna: " + e.getMessage());
		}
		
		try {
			this.certsChain = keyStore.getCertificateChain(this.certificateAlias);
		} 
		catch (KeyStoreException e) {
			throw new Exception("Não foi possível recuperar a cadeia de certificado do certificado PKCS11, mensagem interna: " + e.getMessage());
		}
		
		try {
			this.privateKey = (PrivateKey) keyStore.getKey(this.certificateAlias, null);
		} 
		catch (UnrecoverableKeyException e) {
			throw new Exception("Não foi possível recuperar a Chave Privada! Verifique se a senha esta correta.\n" + e);
		} 
		catch (KeyStoreException e) {
			throw new Exception("Não foi possível recuperar a Chave Privade do certificado PKCS11, mensagem interna: " + e.getMessage());
		} 
		catch (NoSuchAlgorithmException e) {
			throw new Exception("Nao foi possível encontrar o algoritmo durante a recuperação da Chave Privada do certificado PKCS11, mensagem interna: " + e.getMessage());
		}
		
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

	@Override
	public String getDescription() {
		return getTokenConfig().getLibrary() + ", " + getSlot();
	}
}