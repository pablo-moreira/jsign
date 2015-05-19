package com.github.jsign;

import java.io.File;
import java.io.FileInputStream;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.github.jsign.gui.DlgConfiguration;
import com.github.jsign.interfaces.SignLog;
import com.github.jsign.interfaces.SignLogProgress;
import com.github.jsign.interfaces.SignProgress;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.manager.Manager;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.MessageToSign;
import com.github.jsign.model.SignedMessage;

public class JSign implements SignLogProgress {

	private DlgConfiguration dlgConfiguration;
	private Configuration configuration;
	private SignProgress progress;
	private SignLog log;
	private boolean allowsCoSigning;
	private Manager manager = new Manager();

	
	public JSign() throws Exception {
		try {						
			Security.addProvider(new BouncyCastleProvider());
						
			try {
				this.configuration = manager.getConfigurationManager().loadConfigurations();
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				this.configuration = new Configuration();
			}
			
			dlgConfiguration = new DlgConfiguration(null, true, this);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Erro na inicialização do assinador, mensagem interna: " + e.getMessage());
		}
	}
	
	public SignProgress getProgress() {
		return progress;
	}

	public void setProgress(SignProgress progress) {
		this.progress = progress;
	}

	public SignLog getLog() {
		return log;
	}

	public void setLog(SignLog log) {
		this.log = log;
	}
	
	public SignedMessage signFile(File file, boolean attached) throws Exception {
				
		if (file == null) {
			throw new Exception("Por favor, selecione algum arquivo para realizar a assinatura digital!");
		}
	
		MessageToSign messageToSign = new MessageToSign(file.getName(), new FileInputStream(file));
					
		List<SignedMessage> signedData = signMessages(Arrays.asList(messageToSign), attached);
		
		return signedData.get(0);
	}
		
	public List<SignedMessage> signMessages(List<MessageToSign> messages, boolean attached) throws Exception {
		
		KeyStoreHelper storeHelper = initKeyStore();
		
		return getManager().getSignManager().signMessages(storeHelper, messages, attached, isAllowsCoSigning(), this);
	}
		
	public KeyStoreHelper initKeyStore() throws Exception {
		
		if (!configuration.isDefinedKeyStoreHelper()) {
			
			if (configuration.getKeyStoreType() != null) {				
				getManager().getConfigurationManager().retrieveKeyStoreHelperByConfiguration(configuration);					
			}
			
			if (!configuration.isDefinedKeyStoreHelper()) {
			
				showDlgConfiguration(false);	
			
				if (!configuration.isDefinedKeyStoreHelper()) {
					throw new Exception("Por favor, para realizar a assinatura deve-se configurar um certificado digital!");
				}
			}
		}
		
		return configuration.getKeyStoreHelper();
		
//		if (configuration.isTypePKCS12()) {
//
//			if (!configuration.isDefinedPkcs12File()) {
//				throw new Exception("Por favor, para realizar a assinatura utilizando PKCS12, deve-se definir o endereço do arquivo do certificado!");
//			}
//			
//			char[] pkcs12Senha;
//			
//			// Verifica se tem cache de senha do repositorio pkcs 12
//			if (!configuration.isDefinedPkcs12Password()) { 
//							
//				dlgPKCS12Password.iniciar();
//
//				if (dlgPKCS12Password.getReturnStatus() == FrmCertificadoPkcs12Senha.RET_CANCEL) {
//					throw new Exception("Por favor, para realizar a assinatura utilizando PKCS12, deve-se informar a senha do certificado!");
//				}						
//				
//				pkcs12Senha = dlgPKCS12Password.getSenha();				
//			}
//			else {
//				pkcs12Senha = configuration.getPkcs12Password();
//			}
//			
//			PKCS12KeyStoreHelper storeHelperPkcs12 = new PKCS12KeyStoreHelper(new FileInputStream(
//					configuration.getPkcs12File()), 
//					pkcs12Senha
//			);
//			
//			configuration.setPkcs12Password(pkcs12Senha);
//			
//			return null;//storeHelperPkcs12;
//		}
//		else if (configuration.isTypeMSCAPI()) {
//
//			List<X509Certificate> certificados = MSCAPIKeyStoreHelper.getCertificatesAvailable();
//						
//			Collections.sort(certificados, new Comparator<X509Certificate>() {
//				@Override
//				public int compare(X509Certificate item1, X509Certificate item2) {
//					
//					String alias1 = CertificateUtils.getCertificateCN(item1.getSubjectDN().getName());
//					String alias2 = CertificateUtils.getCertificateCN(item2.getSubjectDN().getName());
//
//					return alias1.compareToIgnoreCase(alias2);
//				}
//			});
//						
//			if (certificados.isEmpty()) {
//				throw new Exception("Por favor, para realizar a assinatura utilizando Windows MsCAPI, deve-se cadastrar os certificados!");
//			}
//			else if (certificados.size() == 1) {					
//				return new MSCAPIKeyStoreHelper(certificados.get(0));
//			}
//			else if (this.msCapiCertificate != null) {
//				return new MSCAPIKeyStoreHelper(msCapiCertificate);				
//			}
//			else {
//				dlgSelectCertificate.iniciar(certificados);
//
//				if (dlgSelectCertificate.getReturnStatus() == DlgSelectCertificate.RET_CANCEL) {
//					throw new Exception("Por favor, para realizar a assinatura utilizando Windows MsCAPI, deve-se escolher algum certificado!");
//				}
//
//				this.msCapiCertificate = dlgSelectCertificate.getCertificado();
//				return new MSCAPIKeyStoreHelper(msCapiCertificate);
//			}
//		}
//		else {
//			throw new Exception("Por favor, para realizar a assinatura deve-se configurar o tipo de repositório!");
//		}
	}
	
	public void showDlgConfiguration() {
		showDlgConfiguration(true);
	}
		
	public void showDlgConfiguration(boolean loadKeyStoreHelper) {		
		dlgConfiguration.start(loadKeyStoreHelper);
	}

	public boolean isAllowsCoSigning() {
		return allowsCoSigning;
	}

	public void setAllowedCoSign(boolean allowsCoSigning) {
		this.allowsCoSigning = allowsCoSigning;
	}
	
	@Override
	public void printProgress(String msg) {
        if (progress != null) {
            progress.printProgress(msg);
        }
	}

	@Override
	public void printLogAndProgress(String msg) {
		printProgress(msg);
		printLog(msg);
	}	
	
	@Override
	public void printLog(String msg) {
        if (log != null) {
            log.printLog(msg);
        }
	}

	public Manager getManager() {
		return manager;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
}