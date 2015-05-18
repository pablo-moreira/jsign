package com.github.jsign.manager;

import com.github.jsign.gui.DlgProtectionCallback;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.PKCS12KeyStoreHelper;
import com.github.jsign.model.Configuration;
import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.login.FailedLoginException;

public class PKCS12Manager {
	
	private DlgProtectionCallback dlgProtectionCallback = new DlgProtectionCallback("Insira o PIN:");

	public List<PKCS12AvailableProvider> getAvailableProviders(Configuration configuration) {

		List<PKCS12AvailableProvider> availableProviders = new ArrayList<PKCS12AvailableProvider>();
		
		for (File pkcs12Certificate : configuration.getPkcs12Certificates()) {
			availableProviders.add(new PKCS12AvailableProvider(pkcs12Certificate));
		}

		return availableProviders;
	}

	public List<PKCS12KeyStoreHelper> getKeyStoreHelpers(PKCS12AvailableProvider availableProvider) throws Exception {
		
		List<PKCS12KeyStoreHelper> keyStoreHelpers = new ArrayList<PKCS12KeyStoreHelper>();
		
		KeyStore keyStore = null;
		
		try {
			keyStore = getKeyStore(availableProvider);
		}
		catch (Exception e) {
			if (e.getCause() instanceof FailedLoginException) {
				throw new Exception("O PIN digitado é inválido!");
			}
			else {
				throw new Exception("Erro ao instanciar o repositório PKCS12, mensagem interna: " + e.getMessage());
			}
		}
		
		if (keyStore != null) {
			
			List<X509Certificate> certificatesAvailable = KeyStoreHelper.getCertificatesAvailable(keyStore);

			for (X509Certificate certificate : certificatesAvailable) {
				keyStoreHelpers.add(new PKCS12KeyStoreHelper(availableProvider.getPkcs12Certificate(), dlgProtectionCallback, keyStore, certificate));
			}

			return keyStoreHelpers;
		}
	}
	
	//		catch (KeyStoreException e) {			
//			throw new Exception("O tipo de KeyStore especificado nao esta disponivel para este provedor criptografico!\n" + e);
//		} 
//		catch (NoSuchAlgorithmException e) {
//			throw new Exception("Nao foi possivel utilizar o algoritmo de verificacao de integridade do KeyStore!\n" + e);
//		} 
//		catch (CertificateException e) {
//			throw new Exception("Nao foi possivel carregar algum dos certificados existentes no KeyStore!\n" + e);
//		}
//		catch (IOException e) {
//			if (e.getCause() instanceof BadPaddingException) {
//				throw new Exception("A senha do certificado está incorreta!\n" + e);
//			}
//			else {
//				throw new Exception("Ocorreu um problema ao ler o arquivo de KeyStore especificado!\n" + e);
//			}
//		}
	
	public KeyStore getKeyStore(PKCS12AvailableProvider availableProvider) throws Exception {
		KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(dlgProtectionCallback);
		KeyStore.Builder kb = KeyStore.Builder.newInstance("PKCS12", null, availableProvider.getPkcs12Certificate(), protectionParameter);
		return kb.getKeyStore();
	}
}
