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
import java.util.Collection;
import java.util.List;

import com.github.jsign.model.KeyStoreType;
import com.github.jsign.util.StringUtils;

public class MSCAPIKeyStoreHelper extends KeyStoreHelper {

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
			KeyStore keyStore = getNewKeyStore();
			
			List<X509Certificate> certificatesAvailable = getCertificatesAvailable(keyStore);
			
			return certificatesAvailable;
		}
		catch (Exception e) {
			throw new Exception("Erro ao obter os certificados MSCAPI disponiveis, mensagem interna: " + e.getMessage());
		}
	}
	
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

	public MSCAPIKeyStoreHelper(X509Certificate certificate) throws Exception  {				
		try {
			keyStore = getNewKeyStore();
			
			init(keyStore, certificate);
		}
		catch (KeyStoreException e) {
			throw new Exception("O KeyStore nao foi inicializado corretamente!\n" + e);
		}
	}
	
	public MSCAPIKeyStoreHelper(KeyStore keyStore, X509Certificate certificate) throws Exception  {
		init(keyStore, certificate);
	}
	
	private void init(KeyStore keyStore, X509Certificate certificate) throws Exception  {
		try {
			this.certificateAlias = keyStore.getCertificateAlias(certificate);

			if (StringUtils.isNullOrEmpty(this.certificateAlias)) {
				throw new Exception("Não existe o certificado " + certificate.getSubjectDN() + " no repositório Windows-MsCapi!");
			}
			
			this.keyStore = keyStore;
			this.certificate = certificate;
			this.privateKey = (PrivateKey) keyStore.getKey(this.certificateAlias, null);
			this.certsChain = keyStore.getCertificateChain(this.certificateAlias);
		}
		catch (Exception e) {
			throw new Exception("Erro ao obter certificado!\n" + e);
		}
	}

	@Override
	public KeyStoreType getType() {
		return KeyStoreType.MSCAPI;
	}
}