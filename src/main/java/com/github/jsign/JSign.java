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
		
		KeyStoreHelper keyStoreHelper = null;
		
		if (configuration.isDefinedKeyStoreType()) {
			keyStoreHelper = getManager().getConfigurationManager().loadKeyStoreHelperByConfiguration(configuration);		
		}

		if (keyStoreHelper == null) {
			
			keyStoreHelper = showDlgConfiguration(false);
			
			if (keyStoreHelper == null) {
				throw new Exception("Por favor, para realizar a assinatura deve-se configurar um certificado digital!");
			}			
		}
		
		return keyStoreHelper;
	}
	
	public KeyStoreHelper showDlgConfiguration() {
		return showDlgConfiguration(true);
	}
		
	public KeyStoreHelper showDlgConfiguration(boolean loadKeyStoreHelper) {		
		
		dlgConfiguration.start(loadKeyStoreHelper);
		
		if (dlgConfiguration.getReturnStatus() == DlgConfiguration.RET_OK) {
			return dlgConfiguration.getKeyStoreHelper();
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
}