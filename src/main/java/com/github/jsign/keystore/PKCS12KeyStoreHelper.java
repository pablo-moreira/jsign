
package com.github.jsign.keystore;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import com.github.jsign.gui.DlgProtectionCallback;
import com.github.jsign.model.KeyStoreType;

public class PKCS12KeyStoreHelper extends KeyStoreHelper {

	private File pkcs12Filename;
	
	public PKCS12KeyStoreHelper(KeyStore keyStore, X509Certificate certificate, File pkcs12Certificate, DlgProtectionCallback dlgProctetionCallback) throws Exception {

		super(keyStore, certificate);
			
		try {
			this.privateKey = (PrivateKey) keyStore.getKey(this.certificateAlias, dlgProctetionCallback.getPassword());
		} 
		catch (UnrecoverableKeyException e) {
			throw new Exception("Não foi possível recuperar a Chave Privada! Verifique se a senha esta correta.\n" + e);
		} 
		catch (KeyStoreException e) {
			throw new Exception("Não foi possível recuperar a Chave Privade do certificado PKCS12, mensagem interna: " + e.getMessage());
		} 
		catch (NoSuchAlgorithmException e) {
			throw new Exception("Nao foi possível encontrar o algoritmo durante a recuperação da Chave Privada do certificado PKCS12, mensagem interna: " + e.getMessage());
		}
		
		this.pkcs12Filename = pkcs12Certificate;
	}
 	
	@Override
	public KeyStoreType getType() {
		return KeyStoreType.PKCS12;
	}

	public File getPkcs12Filename() {
		return pkcs12Filename;
	}

	@Override
	public String getDescription() {
		return getPkcs12Filename().getAbsolutePath();
	}
}
