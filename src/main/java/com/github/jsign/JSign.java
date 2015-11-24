package com.github.jsign;

import java.awt.Frame;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.github.jsign.gui.DlgCertificateNotFound;
import com.github.jsign.gui.DlgConfiguration;
import com.github.jsign.gui.DlgConfigurationWindows;
import com.github.jsign.interfaces.SignLog;
import com.github.jsign.interfaces.SignLogProgress;
import com.github.jsign.interfaces.SignProgress;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.MSCAPIKeyStoreHelper;
import com.github.jsign.keystore.PKCS11KeyStoreHelper;
import com.github.jsign.manager.Manager;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.MessageToSign;
import com.github.jsign.model.SignedMessage;
import com.github.jsign.util.FileUtils;
import com.github.jsign.util.JFrameUtils;

public class JSign implements SignLogProgress {

	private DlgConfiguration dlgConfiguration;
	private DlgConfigurationWindows dlgConfigurationWindows;
	private Configuration configuration;
	private SignProgress progress;
	private SignLog log;
	private boolean allowsCoSigning;
	private boolean allowsPkcs12Certificate = true;
	private Manager manager = new Manager();
	private KeyStoreHelper keyStore;
	private DlgCertificateNotFound dlgCertificateNotFound;
	
	public JSign() throws Exception {
		this(null);
	}
	
	public JSign(Frame parent) throws Exception {
		try {						
			Security.addProvider(new BouncyCastleProvider());
						
			try {
				this.configuration = manager.getConfigurationManager().loadConfigurations();
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				this.configuration = new Configuration();
			}
			
			dlgConfiguration = new DlgConfiguration(parent, true, this);
			dlgConfiguration.setAlwaysOnTop(true);
			dlgConfigurationWindows = new DlgConfigurationWindows(parent, true, this);
			dlgConfigurationWindows.setAlwaysOnTop(true);
			dlgCertificateNotFound = new DlgCertificateNotFound(parent, true, this);
			dlgCertificateNotFound.setAlwaysOnTop(true);
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
	
		MessageToSign messageToSign = new MessageToSign(file.getName(), FileUtils.getFileBytes(file));
					
		return signMessage(messageToSign, attached);
	}
	
	/**
	 * Metodo responsavel por assinar um documento, ele nao se responsabiliza por iniciar e fechar o keystore.
	 * 
	 * @param messageToSign Messagem que sera assinada
	 * @param attached Se o envelope gerado tera a mensagem original atachada ou nao
	 * @return O arquivo assinado
	 * @throws Exception
	 */
	public SignedMessage signMessage(MessageToSign messageToSign, boolean attached) throws Exception {
		
		if (this.keyStore == null) {
			throw new RuntimeException("Por favor inicie o keyStore, execute jSign.initKeyStore()");
		}
		
		List<SignedMessage> signMessages = getManager().getSignManager().signMessages(this.keyStore, Arrays.asList(messageToSign), attached, isAllowsCoSigning(), this);
		
		return signMessages.get(0);
	}
	
	/**
	 * Metodo responsavel por iniciar o keyStore, assinar os documentos e deslogar o keystore.
	 *  
	 * @param messages As mensagens que serao assinadas
	 * @param attached Se o envelope gerado tera a mensagem original atachada ou nao
	 * @return Os arquivos assinados
	 * @throws Exception
	 */
	public List<SignedMessage> signMessages(List<MessageToSign> messages, boolean attached) throws Exception {
		
		initKeyStore();
		
		List<SignedMessage> signMessages = getManager().getSignManager().signMessages(this.keyStore, messages, attached, isAllowsCoSigning(), this);

		logoutKeyStore();
		
		return signMessages;
	}
	
	public KeyStoreHelper initKeyStore() throws Exception {

		if (this.keyStore == null) {
			
			KeyStoreHelper keyStoreHelper = null;
			
			// Verifica se existe algum certificado configurado, se houver utiliza o certificado
			if (this.configuration.isDefinedKeyStoreType()) {
				keyStoreHelper = getManager().getConfigurationManager().loadKeyStoreHelperByConfiguration(this.configuration);		
			}
			
			if (keyStoreHelper == null) {
	
				keyStoreHelper = configKeyStore();
				
				if (keyStoreHelper == null) {
					throw new Exception("Por favor, para realizar a assinatura deve-se configurar um certificado digital!");
				}
			}
			
			if (keyStoreHelper instanceof MSCAPIKeyStoreHelper) {
				getManager().getMscapiManager().initMscapiKeyStore((MSCAPIKeyStoreHelper) keyStoreHelper);
			}
			
			this.keyStore = keyStoreHelper;
		}
		else {
			
			if (this.keyStore instanceof MSCAPIKeyStoreHelper) {
								
				if (!this.keyStore.isLogged()) {
					
					String msg = "Deseja autorizar a utilização do dispositivo criptográfico:\n";
					msg+= this.keyStore.getDescription();
					
					int result = JOptionPane.showConfirmDialog(null, msg, "Autorização", JOptionPane.YES_NO_OPTION);
					
					if (JOptionPane.OK_OPTION == result) {
						this.keyStore.login();
					}
					else {
						throw new Exception("A operação não foi autorizada pelo usuário!");
					}
				}				
			}
		}
		
		return this.keyStore;
	}
	
	private KeyStoreHelper configKeyStore() {
		
		// Se o sistema operacional for windows verifica se existe certificados na MSCAPI, se houver retorna lista dos certificados  
		List<MSCAPIKeyStoreHelper> keyStoresHelpersAvailableMsCapi = getManager().getConfigurationManager().getKeyStoresHelpersAvailableOnMsCapi();
		
		KeyStoreHelper keyStoreHelper = null;

		if (!keyStoresHelpersAvailableMsCapi.isEmpty()) {
			keyStoreHelper = showDlgConfigurationWindows(keyStoresHelpersAvailableMsCapi);
		}
		else {
			keyStoreHelper = showDlgCertificateNotFound();
		}
		
		return keyStoreHelper;
	}

	private KeyStoreHelper showDlgCertificateNotFound() {

		dlgCertificateNotFound.start();
		
		if (dlgCertificateNotFound.getReturnStatus() == DlgCertificateNotFound.RET_CERTIFICATE_FOUND) {
			return configKeyStore();
		}
		else if (dlgCertificateNotFound.getReturnStatus() == DlgCertificateNotFound.RET_OPEN_DLG_CONFIGURATION) {
			return showDlgConfiguration();
		}
		else {
			return null;
		}
	}

	public void resetKeyStore() {
		this.keyStore = null;
	}
		
	public KeyStoreHelper showDlgConfiguration() {
		
		dlgConfiguration.start();
		
		if (dlgConfiguration.getReturnStatus() == DlgConfiguration.RET_OK) {
			
			KeyStoreHelper keyStoreHelper = dlgConfiguration.getKeyStoreHelper();
									
			updateKeyStoreHelper(keyStoreHelper);	    	
						
			return keyStoreHelper;
		}
		else {
			return null;
		}
	}

	private void updateKeyStoreHelper(KeyStoreHelper keyStoreHelper) {

		this.configuration.updateKeyStoreHelper(keyStoreHelper);		
		this.keyStore = null;

    	try {
    		getManager().getConfigurationManager().writeConfiguration(this.configuration);
    	}
    	catch (Exception e) {
    		JFrameUtils.showErro("Erro", "Erro ao persistir as configurações!\nMensagem Interna: " + e.getMessage());
    	}
	}

	private KeyStoreHelper showDlgConfigurationWindows(List<MSCAPIKeyStoreHelper> keyStoresHelpersAvailable) {
		
		dlgConfigurationWindows.start(keyStoresHelpersAvailable);
		
		if (dlgConfigurationWindows.getReturnStatus() == DlgConfigurationWindows.RET_FINISH) {
						
			KeyStoreHelper keyStoreHelper = dlgConfigurationWindows.getKeyStoreHelper();
			
			updateKeyStoreHelper(keyStoreHelper);
			
			return keyStoreHelper;
		}
		else if (dlgConfigurationWindows.getReturnStatus() == DlgConfigurationWindows.RET_OPEN_DLG_CONFIGURATION) {
			return showDlgConfiguration();
		}
		else {
			return null;
		}	
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

	public boolean isAllowsPkcs12Certificate() {
		return allowsPkcs12Certificate;
	}

	public void setAllowsPkcs12Certificate(boolean allowsPkcs12Certificate) {
		this.allowsPkcs12Certificate = allowsPkcs12Certificate;
	}

	public void  setDlgConfigurationAlwaysOnTop(boolean value) {
		this.dlgConfiguration.setAlwaysOnTop(true);
	}

	public void writeConfiguration(Configuration configuration) throws Exception {
		this.configuration = configuration;
		this.getManager().getConfigurationManager().writeConfiguration(configuration);
	}
	
	public void clearConfiguration() {
		this.configuration = this.getManager().getConfigurationManager().clearConfiguration();
	}

	public void setUrlDriversInstallationHelpPage(URL url) {
		dlgCertificateNotFound.setUrlDriversInstallationHelpPage(url);
	}
	
	public void setIconImage(Image image) {
		dlgConfiguration.setIconImage(image);
		dlgConfigurationWindows.setIconImage(image);
		dlgCertificateNotFound.setIconImage(image);
	}

	public void logoutKeyStore() {
		
		if (this.keyStore != null && this.keyStore.isLogged()) {					
			
			this.keyStore.logout();
			
			/**
			 * Quando o provider do keystore for pkcs11 a instancia devera ser removida pois nao podera mais ser utilizada apos o logout 
			 */
			if (this.keyStore instanceof PKCS11KeyStoreHelper) {
				this.keyStore = null;
			}
		}
	}
}