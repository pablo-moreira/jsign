package com.github.jsign;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import junit.framework.TestCase;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.model.SignedMessage;
import com.github.jsign.util.FileUtils;


public class JSignTest extends TestCase {
    
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
        
        assertEquals(1, 1);
    }
    
    public static void main(String[] args) throws Exception {
    	
    	final Sign sign = new Sign();

    	//sign.showDlgConfiguration();
    	List<KeyStoreHelper> keyStoreHelpersAvailable = sign.getManager().getConfigurationManager().getKeyStoreHelpersAvailable();

    	System.out.println(keyStoreHelpersAvailable.size());
	}
}
