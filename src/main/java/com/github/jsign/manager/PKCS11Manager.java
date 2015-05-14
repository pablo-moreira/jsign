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

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.PKCS11KeyStoreHelper;
import com.github.jsign.model.OperatingSystem;
import com.github.jsign.model.PKCS11AvailableProvider;
import com.github.jsign.model.PKCS11Tokens;
import com.github.jsign.model.TokenConfig;

public class PKCS11Manager {

	private PKCS11Tokens tokens = new PKCS11Tokens();
	
	public static Provider getProvider(String name, String library, Integer slot) {
		
		Properties map = new Properties();
		
		map.setProperty("name", name);
		map.setProperty("library", library);
		
		if (slot != null) {
			map.setProperty("slot", slot.toString());
		}

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
	
	public Provider tryGetProvider(TokenConfig tokenConfig) {

		File library = new File(tokenConfig.getLibrary());
		
		if (library.exists()) {
			
			try {
				Provider provider = getProvider(tokenConfig.getToken().getName(), tokenConfig.getLibrary(), tokenConfig.getSlot());
					
				Security.addProvider(provider);
				
				return provider;
			}
			catch(ProviderException e){}			
		}
		
		return null;
	}

	public List<PKCS11KeyStoreHelper> getCertificatesAvailable() {
		
		List<PKCS11AvailableProvider> availableProviders = getAvailableProviders();
		
		for (PKCS11AvailableProvider pkcs11AvailableProvider : availableProviders) {
			
				
		}

		return null;
	}

	private List<PKCS11AvailableProvider> getAvailableProviders() {
		
		OperatingSystem operatingSystem = OperatingSystem.getOperatingSystem();
		
		List<TokenConfig> tokensConfigsByOperatingSystem = tokens.getTokensConfigsByOperatingSystem(operatingSystem);
		
		List<PKCS11AvailableProvider> availableProviders = new ArrayList<PKCS11AvailableProvider>(); 
				
		for (TokenConfig tokenConfig : tokensConfigsByOperatingSystem) {
			
			Provider provider = tryGetProvider(tokenConfig);
			
			if (provider != null) {
				availableProviders.add(new PKCS11AvailableProvider(provider, tokenConfig));	
			}
		}
		
		return availableProviders;
	}

	public List<PKCS11KeyStoreHelper> tryGetKeyStoreHelpersAvailable() {

		List<PKCS11AvailableProvider> availableProviders = getAvailableProviders();
		
		List<PKCS11KeyStoreHelper> keyStoreHelpers = new ArrayList<PKCS11KeyStoreHelper>();
		
		for (PKCS11AvailableProvider provider : availableProviders) {
		
						
            try {
            	KeyStore keyStore = KeyStore.getInstance("PKCS11", provider.getProvider());
				keyStore.load(null, null);
			
				List<X509Certificate> certificates = KeyStoreHelper.getCertificatesAvailable(keyStore);
				
				for (X509Certificate certificate : certificates) {
					
					System.out.println(certificate.getSubjectDN().getName());
					
				}
								
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return keyStoreHelpers;
	}
}