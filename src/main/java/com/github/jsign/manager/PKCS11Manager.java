package com.github.jsign.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.github.jsign.gui.PKCS11CallbackHandler;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.PKCS11KeyStoreHelper;
import com.github.jsign.model.OperatingSystem;
import com.github.jsign.model.PKCS11AvailableKeyStore;
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
				
		Properties map = new Properties();
		
		map.setProperty("name", name);
		
		if(library.contains(":/")){
			map.setProperty("library", library.substring(library.lastIndexOf("/") + 1));
		}else if(library.contains(":\\")){
			map.setProperty("library", library.substring(library.lastIndexOf("\\") + 1));
		}else{
			map.setProperty("library", library);
		}
		
		if (slot != null) {
			map.setProperty("slot", slot.toString());
		}
		
		//map.setProperty("showInfo", "true");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try{
			map.store(bos, null);
			
			InputStream bin = new ByteArrayInputStream(bos.toByteArray());
			
			bos.close();
			
			Provider provider = new sun.security.pkcs11.SunPKCS11(bin);
			
			bin.close();
			
			return provider;
		}
		catch (Exception e) {
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
				try {
					Provider provider = getProvider(tokenConfig, null);
					
					Security.addProvider(provider);
					Security.removeProvider(provider.getName());
					
					availableTokenConfigs.add(tokenConfig);
				}
				catch(Exception e) {
					
				}				
			}	
		}

		return availableTokenConfigs;
	}
		
	public List<PKCS11KeyStoreHelper> tryGetKeyStoreHelpersAvailable() {

		List<PKCS11AvailableKeyStore> keyStores = getAvailableKeyStores();				
		List<PKCS11KeyStoreHelper> keyStoreHelpers = new ArrayList<PKCS11KeyStoreHelper>();

		for (PKCS11AvailableKeyStore availableKeyStore : keyStores) {

			try {
				List<X509Certificate> certificatesAvailable = KeyStoreHelper.getCertificatesAvailable(availableKeyStore.getKeyStore());
						
				for (X509Certificate certificate : certificatesAvailable) {					
					try {
						keyStoreHelpers.add(new PKCS11KeyStoreHelper(availableKeyStore.getTokenConfig(), availableKeyStore.getSlot(), callbackHandler, availableKeyStore.getKeyStore(), certificate));
					}
					catch (Exception e) {
						// Skip certificate
					}
				}
			}
			catch (Exception e) {
				
			}
		}
		
		return keyStoreHelpers;
	}

	private List<PKCS11AvailableKeyStore> getAvailableKeyStores() {

		List<TokenConfig> availableTokenConfigs = getAvailableTokenConfigs();
		List<PKCS11AvailableKeyStore> availableKeyStores = new ArrayList<PKCS11AvailableKeyStore>();
		
		for (TokenConfig tokenConfig : availableTokenConfigs) {

			long[] slotList = tryGetSlotListWithToken(tokenConfig);
			
			if (slotList != null) {
				for (long slot : slotList) {

					KeyStore keyStore = tryGetKeyStore(tokenConfig, slot);

					if (keyStore != null) {
						availableKeyStores.add(new PKCS11AvailableKeyStore(tokenConfig, slot, keyStore));
					}
				}
			}
			else {
				// Try brutal force
				for (long slot = 0; slot < 10; slot++) {
					KeyStore keyStore = tryGetKeyStore(tokenConfig, slot);

					if (keyStore != null) {
						availableKeyStores.add(new PKCS11AvailableKeyStore(tokenConfig, slot, keyStore));
					}
				}
			}
		}
		
		return availableKeyStores;
	}

	private long[] tryGetSlotListWithToken(TokenConfig tokenConfig) {
		
		try {
			PKCS11Wrapper instance = PKCS11Wrapper.getInstance(new File(tokenConfig.getLibrary()));
			return instance.getSlotList();
		}
		catch (Exception e) {}
		
		return null;
	}

	private KeyStore tryGetKeyStore(TokenConfig tokenConfig, long slot) {
		
		Provider provider = null;
		
		try {			
			provider = getProvider(tokenConfig, slot);
			
			Security.addProvider(provider);
									
			KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(callbackHandler);
			KeyStore.Builder kb = KeyStore.Builder.newInstance("PKCS11", provider, protectionParameter);
			return kb.getKeyStore();
		}
		catch (Exception e) {						
			return null;
		}		
	}
}