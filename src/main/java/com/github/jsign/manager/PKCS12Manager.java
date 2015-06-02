package com.github.jsign.manager;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;

import com.github.jsign.exceptions.LoginCancelledException;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.PKCS12KeyStoreHelper;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.PKCS12AvailableProvider;
import com.github.jsign.util.ExceptionUtils;

public class PKCS12Manager {
	
	private Manager manager;
	
	public PKCS12Manager(Manager manager) {
		this.manager = manager;
	}
	
	public Manager getManager() {
		return manager;
	}

	public List<PKCS12AvailableProvider> getAvailableProviders(Configuration configuration) {

		List<PKCS12AvailableProvider> availableProviders = new ArrayList<PKCS12AvailableProvider>();
		
		for (File pkcs12Certificate : configuration.getPkcs12Certificates()) {
			availableProviders.add(new PKCS12AvailableProvider(pkcs12Certificate));
		}

		return availableProviders;
	}

	public List<PKCS12KeyStoreHelper> getKeyStoreHelpers(PKCS12AvailableProvider availableProvider) throws Exception {
		
		List<PKCS12KeyStoreHelper> keyStoreHelpers = new ArrayList<PKCS12KeyStoreHelper>();
		
		KeyStore keyStore = getKeyStore(availableProvider);
				
		if (keyStore != null) {
			List<X509Certificate> certificatesAvailable = KeyStoreHelper.getCertificatesAvailable(keyStore);

			for (X509Certificate certificate : certificatesAvailable) {
				keyStoreHelpers.add(new PKCS12KeyStoreHelper(availableProvider.getPkcs12Certificate(), availableProvider.getDlgProtectionCallback(), keyStore, certificate));
			}
		}
		
		return keyStoreHelpers;
	}
		
	public KeyStore getKeyStore(PKCS12AvailableProvider availableProvider) throws Exception {
		
		try {
			KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(availableProvider.getDlgProtectionCallback());
			KeyStore.Builder kb = KeyStore.Builder.newInstance("PKCS12", null, availableProvider.getPkcs12Certificate(), protectionParameter);
			return kb.getKeyStore();	
		}		
		catch (KeyStoreException e) {
			if (e.getCause() instanceof IOException) {
				if (e.getCause().getCause() instanceof BadPaddingException) {
					throw new Exception("O PIN digitado é inválido!");
				}
				else if (ExceptionUtils.checkCauseInstanceOfRecursive(e, LoginCancelledException.class)) {
					throw new Exception("Por favor, é necessário fornecer o PIN para liberar o certifiado digital!");
				}
				else {
					throw new Exception("Ocorreu um problema ao ler o arquivo de PKCS12 especificado!\nMensagem Interna: " + e.getMessage());
				}			
			}
			else {
				throw new Exception("Erro ao instanciar o repositório PKCS12,\nMensagem interna: " + e.getMessage());
			}
		}
	}

	public KeyStoreHelper retrieveKeyStoreHelperByConfiguration(Configuration configuration) throws Exception {
		
		PKCS12AvailableProvider availableProvider = new PKCS12AvailableProvider(configuration.getPkcs12Filename());
		
		KeyStore keyStore = getKeyStore(availableProvider);		
		
		if (keyStore != null) {
			
			X509Certificate certificate = (X509Certificate) keyStore.getCertificate(configuration.getCertificateAlias());
			
			if (certificate != null) {
				try {
					return new PKCS12KeyStoreHelper(availableProvider.getPkcs12Certificate(), availableProvider.getDlgProtectionCallback(), keyStore, certificate);
				}
				catch (Exception e) {
					throw new RuntimeException("Erro ao instanciar o repositorio PKCS12!, mensagem interna: " + e.getMessage());
				}
			}
		}

		return null;
	}
	
	public void addPkcs12Certificate(Configuration configuration, File pkcs12Certificate) throws Exception {
		
		configuration.addPkcs12Certificate(pkcs12Certificate);
		
		getManager().getConfigurationManager().writeConfiguration(configuration);
	}

	public void deletePkcs12Certificate(Configuration configuration, File pkcs12Certificate) throws Exception {		
		
		configuration.getPkcs12Certificates().remove(pkcs12Certificate);
		
		getManager().getConfigurationManager().writeConfiguration(configuration);		
	}
}