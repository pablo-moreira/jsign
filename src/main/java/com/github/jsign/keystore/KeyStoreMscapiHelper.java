package com.github.jsign.keystore;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

public class KeyStoreMscapiHelper extends KeyStoreHelper {

	public static final String TYPE = "Windows-MY";
	public static final String PROVIDER = "SunMSCAPI";	
	
	/**
	 * Codigo para resolver o problema de alias iguais para certificados diferentes
	 * @author http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6672015
	 * @param keyStore
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
		
	public static List<X509Certificate> getCertificatesAvailable() throws Exception {
		try {		
			List<X509Certificate> certificates = new ArrayList<X509Certificate>();
						
			KeyStore keyStore = newKeyStoreInstance();

			Enumeration<String> aliases = keyStore.aliases();

			while (aliases.hasMoreElements()) {
				
				String alias = (String) aliases.nextElement();

				if (keyStore.isKeyEntry(alias)) {
					certificates.add((X509Certificate) keyStore.getCertificate(alias));
				}
			}

			return certificates;
		}
		catch (KeyStoreException e) {
			throw new Exception("O KeyStore nao foi inicializado corretamente!\n" + e);
		}
	}
	
	private static KeyStore newKeyStoreInstance() throws Exception {
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
			throw new Exception("Algoritmo não suportado na CAPI!\n" + e);
		}
		catch (IOException e) {
			throw new Exception("Erro na comunicação com a CAPI!\n" + e);
		}
	}

	public KeyStoreMscapiHelper(X509Certificate certificate) throws Exception  {				
		try {
			keyStore = newKeyStoreInstance();
			
			String alias = keyStore.getCertificateAlias(certificate);

			if (alias == null || alias.isEmpty()) {
				throw new Exception("Não existe o certificado " + certificate.getSubjectDN() + " no repositório Windows-MsCapi!");
			}
			
			this.certificate = certificate;
			this.privateKey = (PrivateKey) keyStore.getKey(alias, null);
			this.certsChain = keyStore.getCertificateChain(alias);
		}
		catch (KeyStoreException e) {
			throw new Exception("O KeyStore nao foi inicializado corretamente!\n" + e);
		}
		catch (Exception e) {
			throw new Exception("Erro ao obter certificado!\n" + e);
		}
	}
}