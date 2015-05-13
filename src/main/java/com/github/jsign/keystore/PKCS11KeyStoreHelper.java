package com.github.jsign.keystore;

import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.CertificateException;

import javax.security.auth.callback.CallbackHandler;

import com.github.jsign.model.TokenConfig;


public class PKCS11KeyStoreHelper extends KeyStoreHelper {
	
	private TokenConfig tokenConfig;
	private Provider provider;
	private CallbackHandler callbackHandler;

	public PKCS11KeyStoreHelper(TokenConfig tokenConfig, Provider provider, CallbackHandler handler) {
		this.tokenConfig = tokenConfig;
		this.provider = provider;
		this.callbackHandler = handler;
	}
	
	public void init() throws Exception {
		try {
			if (keyStore == null) {
				KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(callbackHandler);
				KeyStore.Builder kb = KeyStore.Builder.newInstance("PKCS11", provider, protectionParameter);
				keyStore = kb.getKeyStore();
				keyStore.load(null, null);
			}
		} 
		catch (IOException e) {
			throw new Exception("Erro ao carregar o keystore, mensagem interna: " + e.getMessage());
		} 
		catch (NoSuchAlgorithmException e) {
			throw new Exception("Não possível encontrar o algoritmo ao carregar o keystore, mensagem interna: " + e.getMessage());
		} 
		catch (CertificateException e) {
			throw new Exception("Erro ao carregar o keystore, mensagem interna: " + e.getMessage());
		}
	}
}
