package com.github.jsign.manager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.MSCAPIKeyStoreHelper;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.MSCAPIAvailableProvider;
import com.github.jsign.model.OperatingSystem;

public class MSCAPIManager {

	public static final String TYPE = "Windows-MY";
	public static final String PROVIDER = "SunMSCAPI";
	
	private Manager manager;

	public MSCAPIManager(Manager manager) {
		this.manager = manager;
	}

	public List<MSCAPIKeyStoreHelper> getKeyStoreHelpers(MSCAPIAvailableProvider availableProvider) {
		
		List<MSCAPIKeyStoreHelper> helpers = new ArrayList<MSCAPIKeyStoreHelper>();
		
		try {
			List<X509Certificate> certificatesAvailable = KeyStoreHelper.getCertificatesAvailable(availableProvider.getKeyStore());
				
			for (X509Certificate certificate : certificatesAvailable) {					
				try {
					helpers.add(new MSCAPIKeyStoreHelper(availableProvider.getKeyStore(), certificate));
				}
				catch (Exception e) {
					// Skip certificate
				}
			}
				
			return helpers;
		}
		catch (Exception e) {
			return helpers;
		}
	}
	
	public MSCAPIAvailableProvider getAvailableProvider() {
		
		if (OperatingSystem.isWindows()) {
			
			Provider provider = Security.getProvider(MSCAPIManager.PROVIDER);
			
			if (provider != null) {
			
				try {
					KeyStore keyStore = getNewKeyStore();
					
					List<X509Certificate> certificates = KeyStoreHelper.getCertificatesAvailable(keyStore);
					
					if (certificates.size() > 0) {
						return new MSCAPIAvailableProvider(provider, keyStore, certificates);
					}
				}
				catch (Exception e) {}
			}
		}
		
		return null;
	}

	public MSCAPIKeyStoreHelper retrieveKeyStoreHelperByConfiguration(Configuration configuration) {

		MSCAPIAvailableProvider availableProvider = getAvailableProvider();
		
		if (availableProvider != null) {
			
			X509Certificate certificate = null;
			
			try {
				 certificate = (X509Certificate) availableProvider.getKeyStore().getCertificate(configuration.getCertificateAlias());
			}
			catch (Exception e) {}
			
			if (certificate != null) {
				try {
					return new MSCAPIKeyStoreHelper(availableProvider.getKeyStore(), certificate);
				}
				catch (Exception e) {} 
			}
		}

		return null;
	}

	public Manager getManager() {
		return manager;
	}

	public List<MSCAPIKeyStoreHelper> getKeyStoreHelpersAvailable() {

		MSCAPIAvailableProvider mscapiProvider = getAvailableProvider();
		
		if (mscapiProvider != null) {
			return getKeyStoreHelpers(mscapiProvider);
		}
		else {
			return new ArrayList<MSCAPIKeyStoreHelper>();
		}
	}
	
	/**
	 * Cria um novo keyStore executando o metodo fixAliases, para arrumar o problema dos aliases iguais.
	 * @return o novo KeyStore do Tipo MSCAPI
	 * @throws Exception
	 */
	public static KeyStore getNewKeyStore() throws Exception {
		try {
			KeyStore ks = KeyStore.getInstance(TYPE);
			ks.load(null, null);
			_fixAliases(ks);
			return ks;
		}
		catch (KeyStoreException e) {
			throw new Exception("O KeyStore nao foi inicializado corretamente!\n" + e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new Exception("Algoritmo não suportado na MSCAPI!\n" + e);
		}
		catch (IOException e) {
			throw new Exception("Erro na comunicação com a MSCAPI!\n" + e);
		}
	}
	
	/**
	 * Codigo para resolver o problema de alias iguais para certificados diferentes
	 * @author http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6672015
	 * @param keyStore o KeyStore que sera reparado
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	private static void _fixAliases(KeyStore keyStore) throws Exception {
		
		Field field;
		KeyStoreSpi keyStoreVeritable;

		try {
			field = keyStore.getClass().getDeclaredField("keyStoreSpi");
			field.setAccessible(true);
			keyStoreVeritable = (KeyStoreSpi) field.get(keyStore);
			
			if ("sun.security.mscapi.KeyStore$MY".equals(keyStoreVeritable.getClass().getName())) {
				
				Collection entries;
				String alias, hashCode;
				X509Certificate[] certificates;

				field = keyStoreVeritable.getClass().getEnclosingClass().getDeclaredField("entries");
				field.setAccessible(true);
				entries = (Collection) field.get(keyStoreVeritable);

				for (Object entry : entries) {
					field = entry.getClass().getDeclaredField("certChain");
					field.setAccessible(true);
					certificates = (X509Certificate[]) field.get(entry);

					hashCode = Integer.toString(certificates[0].hashCode());

					field = entry.getClass().getDeclaredField("alias");
					field.setAccessible(true);
					alias = (String) field.get(entry);

					if (!alias.equals(hashCode)) {
						field.set(entry, alias.concat(" - ").concat(hashCode));
					} 
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception(MessageFormat.format("Erro ao realizar o unificação dos alias dos certificados da MsCAPI, {0}", e.getMessage()));
		}
	}
	
	public void initMscapiKeyStore(MSCAPIKeyStoreHelper keyStoreHelper) throws Exception {

		try {			
			Cipher cipher = null;
			
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, keyStoreHelper.getPrivateKey());

			cipher.doFinal("UNLOKED_PIN".getBytes());
		}
		catch (NoSuchAlgorithmException e) {
			throw new Exception("Não foi possível inicializar o token, mensagem interna: " + e.getMessage());
		}
		catch (InvalidKeyException e) {
			throw new Exception("Não foi possível inicializar o token, mensagem interna: " + e.getMessage());
		}
	}
}