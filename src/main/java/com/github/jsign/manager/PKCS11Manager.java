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

import com.github.jsign.gui.DlgProtectionCallback;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.PKCS11KeyStoreHelper;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.OperatingSystem;
import com.github.jsign.model.PKCS11AvailableProvider;
import com.github.jsign.model.PKCS11Tokens;
import com.github.jsign.model.Token;
import com.github.jsign.model.TokenConfig;
import com.github.jsign.util.FileUtils;
import com.github.jsign.util.PKCS11Wrapper;

public class PKCS11Manager {

	private PKCS11Tokens tokens = new PKCS11Tokens();
	private DlgProtectionCallback callbackHandler = new DlgProtectionCallback();
	private Manager manager;
	
	public PKCS11Manager(Manager manager) {
		this.manager = manager;
	}
	
	public Manager getManager() {
		return manager;
	}

	public Provider getProvider(TokenConfig tokenConfig, Long slot) {
		return getProvider(tokenConfig.getToken().getName(), tokenConfig.getLibrary(), slot);
	}
	
	public Provider getProvider(String name, String library, Long slot) {
				
		StringBuilder properties = new StringBuilder();
		properties.append("name=").append(name).append("\n");		
		properties.append("library=");
		
		if (OperatingSystem.isWindows()) {
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

	public List<PKCS11AvailableProvider> getAvailableProviders(Configuration configuration) {

		List<PKCS11AvailableProvider> availableProviders = new ArrayList<PKCS11AvailableProvider>();
		
		List<TokenConfig> availableTokenConfigs = getAvailableTokenConfigs();
		
		List<File> pkcs11Drivers = configuration.getPkcs11Drivers();
				
		for (File file : pkcs11Drivers) {
			Token token = new Token(FileUtils.getFilenameWithoutExtension(file.getName()));
			token.addLib(OperatingSystem.getOperatingSystem(), file.getAbsolutePath());
			availableTokenConfigs.add(token.getConfigs().get(0));
		}

		for (TokenConfig tokenConfig : availableTokenConfigs) {

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

	public List<PKCS11KeyStoreHelper> getKeyStoreHelpers(PKCS11AvailableProvider availableProvider) throws Exception {
		
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
			KeyStore keyStore = null;
			
			try {
				keyStore = getKeyStore(availableProvider.getProvider());
			}
			catch (Exception e) {
				if (e.getCause() instanceof FailedLoginException) {
					throw new Exception("O PIN digitado é inválido!");
				}
			}
			
			availableProvider.setKeyStore(keyStore);			
			availableProviders.add(availableProvider);
		}
		
		for (PKCS11AvailableProvider avProvider : availableProviders) {
			try {
				List<X509Certificate> certificatesAvailable = KeyStoreHelper.getCertificatesAvailable(avProvider.getKeyStore());
						
				for (X509Certificate certificate : certificatesAvailable) {
					helpers.add(new PKCS11KeyStoreHelper(avProvider.getTokenConfig(), avProvider.getSlot(), avProvider.getKeyStore(), certificate));
				}
			}
			catch (Exception e) {
				
			}			
		}
		
		return helpers;
	}
	
	private KeyStore getKeyStore(Provider provider) throws KeyStoreException {		
		KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(callbackHandler);
		KeyStore.Builder kb = KeyStore.Builder.newInstance("PKCS11", provider, protectionParameter);
		return kb.getKeyStore();
	}

	public void addPkcs11Driver(Configuration configuration, File pkcs11Driver) throws Exception {
		
		configuration.addPkcs11Driver(pkcs11Driver);
		
		getManager().getConfigurationManager().writeConfiguration(configuration);
	}

	public void deletePkcs11Driver(Configuration configuration, File pkcs11Driver) throws Exception {
		
		configuration.getPkcs11Drivers().remove(pkcs11Driver);
		
		getManager().getConfigurationManager().writeConfiguration(configuration);
	}
	
	public KeyStoreHelper retrieveKeyStoreHelperByConfiguration(Configuration configuration) {

//		PKCS11AvailableProvider ap11 = (PKCS11AvailableProvider) ap;
//
//		if (ap11.getTokenConfig().getToken().getName().equals(configuration.getPkcs11Name())
//				&& ap11.getTokenConfig().getLibrary().equals(configuration.getPkcs11Library())   	    					
//				&& (ap11.getSlot() != null && ap11.getSlot().equals(configuration.getPkcs11Slot())
//					|| ap11.getSlot() == null && configuration.getPkcs11Slot() == null)) {
//			
//		}
		
		return null;
	}
}