package com.github.jsign.manager;

import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.github.jsign.interfaces.SignLogProgress;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.model.MessageToSign;
import com.github.jsign.model.SignedMessage;
import com.github.jsign.util.FileUtils;

public class SignManager {

	private Manager manager;

	public SignManager(Manager manager) {
		this.manager = manager;
	}
		
	public Manager getManager() {
		return manager;
	}
	
	public boolean isSignedData(byte[] data) {
		try {
			CMSSignedData pkcs7 = new CMSSignedData(data);
			pkcs7.getSignedContent();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public List<SignedMessage> signMessages(KeyStoreHelper storeHelper, List<MessageToSign> messages, boolean attached, boolean allowsCoSigning, SignLogProgress logProgress) throws Exception { 

		if (messages == null || messages.isEmpty()) {
			throw new Exception("Por favor, selecione alguma mensagem para realizar a assinatura digital!");
		}
		
		logProgress.printLogAndProgress("Iniciando o processo de assinatura...");

		logProgress.printLog("O tipo de assinatura é \"" + (attached ? "atachada" : "detachada") + "\"");

		Certificate[] certsChain = storeHelper.getCertsChain();
	
		List<Certificate> certList = new ArrayList<Certificate>();

		logProgress.printLogAndProgress("Carregando a cadeia de certificados...");
		
		if (certsChain != null && certsChain.length != 0) {
            certList.addAll(Arrays.asList(certsChain));
		}
		else {
			certList.add(storeHelper.getCertificate());
		}

		CertStore certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), BouncyCastleProvider.PROVIDER_NAME);
		
		logProgress.printLogAndProgress("Iniciando a assinatura");
		
		int i=1;
		
		// Verifica se permite co-assinatura
		if (!allowsCoSigning) {

			// Verifica se tem algum arquivo ja assinado
			for (MessageToSign message : messages) {
	
				byte[] mensagem = FileUtils.getInputStreamBytes(message.getMessage());
	
				if (isSignedData(mensagem)) {
					throw new Exception (MessageFormat.format("O assinador não suporta co-assinatura! o arquivo ({0}) já foi assinado!", message.isDefinedName() ? message.getName() : i));
				}
				
				i++;
			}
		}
		
		List<SignedMessage> signedMessages = new ArrayList<SignedMessage>();

		for (MessageToSign message : messages) {
			signedMessages.add(signMessage(storeHelper, certs, attached, logProgress, message));
		}
		
		return signedMessages;
	}
	
	private SignedMessage signMessage(KeyStoreHelper keyStoreHelper, CertStore certs, boolean attached, SignLogProgress logProgress, MessageToSign messageToSign) throws Exception {

		logProgress.printLogAndProgress("Assinando: " + messageToSign.getName());
		
		try {
			CMSProcessable msg = new CMSProcessableByteArray(FileUtils.getInputStreamBytes(messageToSign.getMessage()));

			CMSSignedDataGenerator gen = new CMSSignedDataGenerator();                
			gen.addSigner(keyStoreHelper.getPrivateKey(), keyStoreHelper.getCertificate(), CMSSignedDataGenerator.DIGEST_SHA1);
			gen.addCertificatesAndCRLs(certs);

			CMSSignedData signedData = gen.generate(msg, attached, keyStoreHelper.getKeyStore().getProvider().getName());

			ContentInfo contentInfo = signedData.getContentInfo(); 
					
			logProgress.printLog("Gravando arquivo assinado: ");
						
			byte[] bytes = contentInfo.getDEREncoded();
			
			logProgress.printLog("Arquivo assinado gravado");
			
			return new SignedMessage(messageToSign, bytes);
		}
		catch (OutOfMemoryError e) {
			// Se o arquivo for muito grande poderar acontecer um java heap space
			throw new Exception(e);
		}
	}
}
