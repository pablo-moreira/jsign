package com.github.jsign.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.github.jsign.gui.PKCS11CallbackHandler;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.PKCS11KeyStoreHelper;
import com.github.jsign.model.OperatingSystem;
import com.github.jsign.model.PKCS11Tokens;
import com.github.jsign.model.TokenConfig;

public class PKCS11Manager {

	private PKCS11Tokens tokens = new PKCS11Tokens();
	private PKCS11CallbackHandler callbackHandler;
	
	public Provider getProvider(TokenConfig tokenConfig, Integer slot) {
		return getProvider(tokenConfig.getToken().getName(), tokenConfig.getLibrary(), slot);
	}
	
	public Provider getProvider(String name, String library, Integer slot) {
				
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
					Provider provider = getProvider(tokenConfig, tokenConfig.getSlot());
					
					Security.addProvider(provider);
					Security.removeProvider(provider.getName());
					
					availableTokenConfigs.add(tokenConfig);
				}
				catch(ProviderException e) {
					
				}
			}	
		}

		return availableTokenConfigs;
	}
		
	public List<PKCS11KeyStoreHelper> tryGetKeyStoreHelpersAvailable() {

		List<TokenConfig> availableTokenConfigs = getAvailableTokenConfigs();
		
		List<PKCS11KeyStoreHelper> keyStoreHelpers = new ArrayList<PKCS11KeyStoreHelper>();
		
		callbackHandler = new PKCS11CallbackHandler("Insira o PIN:");
		
		for (TokenConfig tokenConfig : availableTokenConfigs) {
			
			for (int slot=0; slot < 9; slot++) {
				
				KeyStore keyStore = tryGetKeyStore(tokenConfig, slot);
				
				keyStoreHelpers.add(new PKCS11KeyStoreHelper(tokenConfig, keyStore));
			}
		}
		
		return keyStoreHelpers;
	}

	private KeyStore tryGetKeyStore(TokenConfig tokenConfig, int slot) {
		try {			
			Provider provider = getProvider(tokenConfig, slot);
									
			KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(callbackHandler);
			KeyStore.Builder kb = KeyStore.Builder.newInstance("PKCS11", provider, protectionParameter);
			return kb.getKeyStore();
		}
		catch (Exception e) {
			return null;
		}		
	}
}