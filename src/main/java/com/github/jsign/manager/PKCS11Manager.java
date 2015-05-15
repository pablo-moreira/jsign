package com.github.jsign.manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.FailedLoginException;

import com.github.jsign.gui.PKCS11CallbackHandler;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.PKCS11KeyStoreHelper;
import com.github.jsign.model.OperatingSystem;
import com.github.jsign.model.PKCS11AvailableProvider;
import com.github.jsign.model.PKCS11Tokens;
import com.github.jsign.model.TokenConfig;
import com.github.jsign.util.PKCS11Wrapper;

public class PKCS11Manager {

	private PKCS11Tokens tokens = new PKCS11Tokens();
	private PKCS11CallbackHandler callbackHandler = new PKCS11CallbackHandler("Insira o PIN:");
	
	public Provider getProvider(TokenConfig tokenConfig, Long slot) {
		return getProvider(tokenConfig.getToken().getName(), tokenConfig.getLibrary(), slot);
	}
	
	public Provider getProvider(String name, String library, Long slot) {
				
		StringBuilder properties = new StringBuilder();
		properties.append("name=").append(name).append("\n");		
		properties.append("library=");
		
		if (OperatingSystem.is64Bit()) {
			properties.append(library);	
		}
		else {
			if(library.contains(":/")){
				properties.append(library.substring(library.lastIndexOf("/") + 1));
			}
			else if(library.contains(":\\")){
				properties.append(library.substring(library.lastIndexOf(":\\")));
			} 
			else{
				properties.append(library);
			}
		}
		properties.append("\n");
				
		if (slot != null) {
			properties.append("slot=").append(slot.toString()).append("\n");
		}
		
		//map.setProperty("showInfo", "true");
		
		try {			
			InputStream bin = new ByteArrayInputStream(properties.toString().getBytes());

			Provider provider = new sun.security.pkcs11.SunPKCS11(bin);
			
			bin.close();
			
			return provider;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(MessageFormat.format("Erro ao instanciar o provider do token {0}, mensagem interna: {1}", name, e.getMessage()));
		}
	}
		
	private List<TokenConfig> getAvailableTokenConfigs() {

		List<TokenConfig> availableTokenConfigs = new ArrayList<TokenConfig>();
		
		OperatingSystem operatingSystem = OperatingSystem.getOperatingSystem();

		List<TokenConfig> tokensConfigsByOperatingSystem = tokens.getTokensConfigsByOperatingSystem(operatingSystem);

		for (TokenConfig tokenConfig : tokensConfigsByOperatingSystem) {
			
			File library = new File(tokenConfig.getLibrary());

			if (library.exists()) {		
				availableTokenConfigs.add(tokenConfig);				
			}	
		}

		return availableTokenConfigs;
	}

	private long[] tryGetSlotListWithToken(TokenConfig tokenConfig) {
		
		try {
			PKCS11Wrapper instance = PKCS11Wrapper.getInstance(new File(tokenConfig.getLibrary()));			
			return instance.getSlotList();
		}
		catch (Exception e) {}
		
		return null;
	}

	public List<PKCS11AvailableProvider> getAvailableProviders() {

		List<PKCS11AvailableProvider> availableProviders = new ArrayList<PKCS11AvailableProvider>();
		
		for (TokenConfig tokenConfig : getAvailableTokenConfigs()) {

			long[] slotList = tryGetSlotListWithToken(tokenConfig);
			
			if (slotList != null) {
				for (long slot : slotList) {

					Provider provider = tryGetProvider(tokenConfig, slot);
					
					if (provider != null) {
						availableProviders.add(new PKCS11AvailableProvider(provider, tokenConfig, slot));
					}
				}
			}
			else {
				availableProviders.add(new PKCS11AvailableProvider(null, tokenConfig, (Long) null));
			}
		}
		
		return availableProviders;
	}

	private Provider tryGetProvider(TokenConfig tokenConfig, long slot) {
		try {
			Provider provider = getProvider(tokenConfig, slot);

			Security.addProvider(provider);
			
			return provider;
		}
		catch (Exception e) {
			return null;
		}
	}

	public List<PKCS11KeyStoreHelper> getKeyStoreHelpders(PKCS11AvailableProvider availableProvider) throws Exception {
		
		List<PKCS11KeyStoreHelper> helpers = new ArrayList<PKCS11KeyStoreHelper>();

		List<PKCS11AvailableProvider> availableProviders = new ArrayList<PKCS11AvailableProvider>();
		
		if (availableProvider.getProvider() == null) {
			// Try brutal force
			for (long slot = 0; slot < 10; slot++) {

				Provider provider = tryGetProvider(availableProvider.getTokenConfig(), slot);
				
				if (provider != null) {
					
					KeyStore keyStore = null;
					
					try {
						keyStore = getKeyStore(provider);
					}
					catch (Exception e) {
						if (e.getCause() instanceof FailedLoginException) {
							throw new Exception("O PIN digitado é inválido!");
						}
					}
				
					if (keyStore != null) {
						availableProviders.add(new PKCS11AvailableProvider(provider, availableProvider.getTokenConfig(), slot, keyStore));
					}
				}
			}			
		}
		else {			
			KeyStore keyStore = tryGetKeyStore(availableProvider.getProvider());
			
			if (keyStore != null) {
				availableProvider.setKeyStore(keyStore);			
				availableProviders.add(availableProvider);
			}
		}
		
		for (PKCS11AvailableProvider avProvider : availableProviders) {
			try {
				List<X509Certificate> certificatesAvailable = KeyStoreHelper.getCertificatesAvailable(avProvider.getKeyStore());
						
				for (X509Certificate certificate : certificatesAvailable) {
					helpers.add(new PKCS11KeyStoreHelper(avProvider.getTokenConfig(), avProvider.getSlot(), callbackHandler, avProvider.getKeyStore(), certificate));
				}
			}
			catch (Exception e) {
				
			}			
		}
		
		return helpers;
	}
	
	private KeyStore tryGetKeyStore(Provider provider) {		
		try {
			return getKeyStore(provider);			
		}		
		catch (Exception e) {
			return null;
		}
	}

	private KeyStore getKeyStore(Provider provider) throws KeyStoreException {		
		KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(callbackHandler);
		KeyStore.Builder kb = KeyStore.Builder.newInstance("PKCS11", provider, protectionParameter);
		return kb.getKeyStore();
	}
}