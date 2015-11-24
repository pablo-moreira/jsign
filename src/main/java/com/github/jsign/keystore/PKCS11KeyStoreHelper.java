package com.github.jsign.keystore;

import java.security.AuthProvider;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

import com.github.jsign.model.KeyStoreType;
import com.github.jsign.model.TokenConfig;


public class PKCS11KeyStoreHelper extends KeyStoreHelper {

	private static Logger logger = Logger.getLogger(PKCS11KeyStoreHelper.class);
	
	private TokenConfig tokenConfig;
	private long slot;
	
	public PKCS11KeyStoreHelper(KeyStore keyStore, X509Certificate certificate, TokenConfig tokenConfig, long slot) throws Exception {

		super(keyStore, certificate);
		
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
		this.logged = true;
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

	@Override
	public void logout() {
		
		try {
			Provider provider = getKeyStore().getProvider();
			
			if (provider instanceof AuthProvider) {
				AuthProvider authProvider = (AuthProvider) provider;
				authProvider.logout();
			}
			
			super.logout();
		}
		catch (Exception e) {
			logger.error("Erro ao fechar o keyStore, mensagem interna: " + e.getMessage(), e);		
		}		
	}
}