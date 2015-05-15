package com.github.jsign;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.model.AvailableProvider;
import com.github.jsign.model.SignedMessage;
import com.github.jsign.util.FileUtils;
import com.github.jsign.util.JFrameUtils;


public class JSignTest {
    
    public static void testSignHugeFile() throws Exception {
    
        final Sign sign = new Sign();

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
    
    public static void main(String[] args) throws Exception {

    	testAvailableProviders();
    	
	}

	private static void testAvailableProviders() throws Exception {
		
		final Sign sign = new Sign();

    	List<AvailableProvider> availableProviders = sign.getManager().getConfigurationManager().getAvailableProviders();
    	
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
	    			helpers = sign.getManager().getConfigurationManager().getKeyStoresHelpersAvailable(availableProvider);	
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
