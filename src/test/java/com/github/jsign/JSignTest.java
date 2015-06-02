package com.github.jsign;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


import com.github.jsign.gui.FrmTest;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.model.AvailableProvider;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.MessageToSign;
import com.github.jsign.model.SignedMessage;
import com.github.jsign.util.FileUtils;
import com.github.jsign.util.JFrameUtils;


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
    		testGui();
		}
    	catch (Exception e) {
			JFrameUtils.showErro("Erro", e.getMessage());
		}
	}
    
    public static void testGui() {
		try {						
			FrmTest frm = new FrmTest();
			frm.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}    	
    }
    
    public static void testSignAndSignAgainPasswordAsk() throws Exception {
    	
    	JSign jSign = new JSign();
		
		jSign.showDlgConfiguration();
    	
    	List<MessageToSign> messages = new ArrayList<MessageToSign>();
    	
    	messages.add(new MessageToSign("file1.txt", new ByteArrayInputStream("Test File 1".getBytes())));
    	messages.add(new MessageToSign("file2.txt", new ByteArrayInputStream("Test File 2".getBytes())));
    	
    	List<SignedMessage> signedMessages = jSign.signMessages(messages, true);
    	
    	System.out.println("Sign 1");
    	
    	List<MessageToSign> messages2 = new ArrayList<MessageToSign>();
    	
    	messages2.add(new MessageToSign("file3.txt", new ByteArrayInputStream("Test File 3".getBytes())));
    	messages2.add(new MessageToSign("file4.txt", new ByteArrayInputStream("Test File 4".getBytes())));

    	// Deve pedir a senha novamente
    	jSign.signMessages(messages2, true); 
    	
    	System.out.println("Sign 2");
    }

	public static void testAvailableProviders() throws Exception {
		
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
