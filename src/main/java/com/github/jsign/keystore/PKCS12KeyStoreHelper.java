
package com.github.jsign.keystore;

import com.github.jsign.gui.DlgProtectionCallback;
import com.github.jsign.model.KeyStoreType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;

public class PKCS12KeyStoreHelper extends KeyStoreHelper {

	private File pkcs12Certificate;
	private DlgProtectionCallback dlgProctetionCallback;
	
	public PKCS12KeyStoreHelper(File pkcs12Certificate, DlgProtectionCallback dlgProctetionCallback, KeyStore keyStore, X509Certificate certificate) {
		this.pkcs12Certificate = pkcs12Certificate;
		this.dlgProctetionCallback = dlgProctetionCallback;
		this.keyStore = keyStore;
		this.certificate = certificate;
	}

	private void init(char[] password) throws Exception {
		
		try{
			boolean keyEntryFound = false;
			String keyEntry;

			Enumeration<String> aliases = this.keyStore.aliases();
				
			do {				
				keyEntry = aliases.nextElement();

				if(keyStore.isKeyEntry(keyEntry)){
					keyEntryFound = true;
					break;
				}
			}
			while(aliases.hasMoreElements());
			
			if(keyEntryFound) {
				this.certificate = (X509Certificate) keyStore.getCertificate(keyEntry);
				this.privateKey = (PrivateKey) keyStore.getKey(keyEntry, password);
				this.certsChain = keyStore.getCertificateChain(keyEntry);				
			} 
			else {
				throw new Exception("Nao foi encontrado nenhum certificado principal no KeyStore!");
			}
			
		} 
		catch (KeyStoreException e) {			
			throw new Exception("O KeyStore nao foi inicializado corretamente!\n" + e);			
		} 
		catch (NoSuchAlgorithmException e) {			
			throw new Exception("Nao foi possivel encontrar o algoritmo de recuperacao" +
											 "de Chave Privada para o provedor criptografico " +
											 "especificado\n" + e);			
		} 
		catch (UnrecoverableKeyException e){
			throw new Exception("Nao foi possivel recuperar a Chave Privada! Verifique se a senha esta correta.\n" + e);
		}
	}
	
	public PKCS12KeyStoreHelper(InputStream inputStream, char[] password) throws Exception {
		
		try{
			keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(inputStream, password);

			init(password);			
		} 
		catch (KeyStoreException e) {			
			throw new Exception("O tipo de KeyStore especificado nao esta disponivel para este provedor criptografico!\n" + e);
		} 
		catch (NoSuchAlgorithmException e) {
			throw new Exception("Nao foi possivel utilizar o algoritmo de verificacao de integridade do KeyStore!\n" + e);
		} 
		catch (CertificateException e) {
			throw new Exception("Nao foi possivel carregar algum dos certificados existentes no KeyStore!\n" + e);
		}
		catch (IOException e) {
			if (e.getCause() instanceof BadPaddingException) {
				throw new Exception("A senha do certificado está incorreta!\n" + e);
			}
			else {
				throw new Exception("Ocorreu um problema ao ler o arquivo de KeyStore especificado!\n" + e);
			}
		}
	}
	
	@Override
	public String getType() {
		return KeyStoreType.PKCS12.name();
	}
}
