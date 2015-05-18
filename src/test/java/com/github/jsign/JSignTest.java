package com.github.jsign;


import com.github.jsign.gui.DlgProtectionCallback;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.model.AvailableProvider;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.SignedMessage;
import com.github.jsign.util.FileUtils;
import com.github.jsign.util.JFrameUtils;
import java.security.KeyStore;
import java.util.Enumeration;


public class JSignTest {
    
    public static void testSignHugeFile() throws Exception {
    
        final JSign sign = new JSign();

        sign.showDlgConfiguration();
        
        Runtime runtime = Runtime.getRuntime();

        System.out.println("Processadores:" + runtime.availableProcessors());
        System.out.println("Memoria em uso: " + FileUtils.getTamanhoFormatado(runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("Memoria livre: " + FileUtils.getTamanhoFormatado(runtime.freeMemory()));
        System.out.println("Total de memoria: " + FileUtils.getTamanhoFormatado(runtime.totalMemory()));
        System.out.println("Maximo de memoria: " + FileUtils.getTamanhoFormatado(runtime.maxMemory()));
        System.out.println("----------------------------------------------------");
        
        SignedMessage signedMessage = sign.signFile(new File("/home/pablo-moreira/Desktop/teste2.tar.gz"), true);
        
        FileOutputStream fos = new FileOutputStream("/home/pablo-moreira/Desktop/teste2.tar.gz.p7s");
        fos.write(signedMessage.getSignedMessage());
        fos.close();
        
        System.out.println("arquivo assinado com sucesso!");
        
        System.out.println("Memoria em uso: " + FileUtils.getTamanhoFormatado(runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("Memoria livre: " + FileUtils.getTamanhoFormatado(runtime.freeMemory()));
        System.out.println("Total de memoria: " + FileUtils.getTamanhoFormatado(runtime.totalMemory()));
        System.out.println("Maximo de memoria: " + FileUtils.getTamanhoFormatado(runtime.maxMemory()));
        
        System.out.println("teste");
    }
    	
    public static void main(String[] args) {

		try {
			DlgProtectionCallback callbackHandler = new DlgProtectionCallback("Insira o PIN:");

			KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(callbackHandler);

			KeyStore.Builder kb = KeyStore.Builder.newInstance("PKCS12", null, new File("C:\\Advogado_18979.p12"), protectionParameter);
			KeyStore keyStore = kb.getKeyStore();	
			
			boolean keyEntryFound = false;
			String keyEntry;
			Enumeration<String> aliases = keyStore.aliases();
			
			while(aliases.hasMoreElements()) {
				
				keyEntry = aliases.nextElement();
				
				System.out.println(keyEntry);
				
				if(keyStore.isKeyEntry(keyEntry)){
					keyEntryFound = true;
				}
			}
			
//			if(keyEntryFound) {
//				this.certificate = (X509Certificate) keyStore.getCertificate(keyEntry);
//				this.privateKey = (PrivateKey) keyStore.getKey(keyEntry, password);
//				this.certsChain = keyStore.getCertificateChain(keyEntry);				
//			} 
//			else {
//				throw new Exception("Nao foi encontrado nenhum certificado principal no KeyStore!");
//			}
			
			
			//FrmTest frm = new FrmTest();
			//frm.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void testAvailableProviders() throws Exception {
		
		final JSign jSign = new JSign();

    	List<AvailableProvider> availableProviders = jSign.getManager().getConfigurationManager().getAvailableProviders(new Configuration());
    	
    	for (AvailableProvider availableProvider : availableProviders) {
    		System.out.println("-------------------------------------");
			System.out.println(availableProvider.getType());
			System.out.println(availableProvider.getDescription());
		}

    	if (availableProviders.size() > 1) {
    		
    		AvailableProvider availableProvider = availableProviders.get(1);    	
	
	    	boolean ok = false;
	    	List<KeyStoreHelper> helpers = new ArrayList<KeyStoreHelper>();
	
	    	while (ok == false) {
	    		try {
	    			helpers = jSign.getManager().getConfigurationManager().getKeyStoresHelpersAvailable(availableProvider);	
	    			ok = true;
	    		}
	    		catch (Exception e) {
	    			JFrameUtils.showErro("Erro", e.getMessage());
	    		}
	    	}
	    	   
	    	for (KeyStoreHelper keyStoreHelper : helpers) {
	    		System.out.println("-------------------------------------");
				System.out.println("KeyStoreType:" + keyStoreHelper.getKeyStore().getType());
				System.out.println("Certificado:" + keyStoreHelper.getCertificate().getSubjectDN().getName());
			}
    	}
		
	}
}
