package com.github.jsign;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableFile;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.github.jsign.gui.FrmCertificadoPkcs12Senha;
import com.github.jsign.gui.FrmSelecionarCertificadoMscapi;
import com.github.jsign.gui.FrmTipoRepositorio;
import com.github.jsign.interfaces.SignLog;
import com.github.jsign.interfaces.SignProgress;
import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.MSCAPIKeyStoreHelper;
import com.github.jsign.keystore.PKCS12KeyStoreHelper;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.MessageToSign;
import com.github.jsign.model.SignedMessage;
import com.github.jsign.util.CertificateUtils;
import com.github.jsign.util.FileUtils;
import com.github.jsign.util.JFrameUtils;

public class Sign {

	private FrmCertificadoPkcs12Senha frmCertificadoPkcs12Senha;
	private FrmSelecionarCertificadoMscapi frmSelecionarCertificadoMscapi;
	private FrmTipoRepositorio dlgKeyStoreType;
	private Configuration repository;
	private SignProgress progresso;
	private SignLog log;
	private X509Certificate msCapiCertificado;
	
	public Sign() throws Exception {
		try {						
			Security.addProvider(new BouncyCastleProvider());

			if (Security.getProvider(MSCAPIKeyStoreHelper.PROVIDER) == null) {
				System.out.println(MessageFormat.format("O provider {0} não esta instalado", MSCAPIKeyStoreHelper.PROVIDER));
			}		

			try {
				loadConfigurations();
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
			frmSelecionarCertificadoMscapi = new FrmSelecionarCertificadoMscapi(null, true);
			frmCertificadoPkcs12Senha = new FrmCertificadoPkcs12Senha(null, true);
			dlgKeyStoreType = new FrmTipoRepositorio(null, true);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Erro na inicialização do assinador, mensagem interna: " + e.getMessage());
		}
	}
	
	public SignProgress getProgresso() {
		return progresso;
	}

	public void setProgresso(SignProgress progresso) {
		this.progresso = progresso;
	}

	public SignLog getLog() {
		return log;
	}

	public void setLog(SignLog log) {
		this.log = log;
	}

	private void imprimirLogEhProgresso(String msg) {
                if (progresso != null) {
                    progresso.printProgress(msg);
                }
		imprimirLog(msg);
	}
		
	private Preferences getPreferences(String path) throws BackingStoreException {
		
		String[] pathItens = path.split("/");
		
		Preferences preferences = Preferences.userRoot();

		for (String pathItem : pathItens) {
			preferences = preferences.node(pathItem);
		}		
		return preferences;
	}
	
	private void loadConfigurations() throws Exception {

		try {
			// Verifica se encontra as configuracoes salvas nas preferencias
			Preferences preferences = getPreferences(SignConstants.PATH);

			if (preferences != null && preferences.keys().length != 0) {
					
				String type = preferences.get(SignConstants.KEY_KEYSTORE_TYPE, "false");
					
				if (Configuration.KEY_STORE_TYPE_MSCAPI.equals(type) || Configuration.KEY_STORE_TYPE_PKCS11.equals(type) || Configuration.KEY_STORE_TYPE_PKCS12.equals(type)) {

					repository = new Configuration();
					repository.setType(type);

					InputStream pkcs12File = new InputStream(preferences.get(SignConstants.KEY_PKCS12_FILENAME, "false"));
					
					if (pkcs12File.isFile()) {													
						repository.setPkcs12File(pkcs12File);
					}
				}
			}		
		}
		catch (Exception e) {
			throw new Exception ("Erro ao ler as configurações!");
		}
	}

	private SignedMessage signMessage(KeyStoreHelper keyStoreHelper, CertStore certs, boolean attached, InputStream message) throws Exception {

		imprimirLogEhProgresso("Assinando: " + message.getName());
		
		try {
			CMSProcessable msg = new CMSProcessableFile(message);

			CMSSignedDataGenerator gen = new CMSSignedDataGenerator();                
			gen.addSigner(keyStoreHelper.getPrivateKey(), keyStoreHelper.getCertificate(), CMSSignedDataGenerator.DIGEST_SHA1);
			gen.addCertificatesAndCRLs(certs);

			CMSSignedData signedData = gen.generate(msg, attached, keyStoreHelper.getKeyStore().getProvider().getName());

			ContentInfo contentInfo = signedData.getContentInfo(); 
                
			InputStream dirTmp = new InputStream(System.getProperty("java.io.tmpdir"));

			InputStream arquivoAssinado = new InputStream(dirTmp, message.getName() + ".p7s");

			imprimirLog("Gravando arquivo assinado: " + arquivoAssinado.getName());

			OutputStream out = new FileOutputStream(arquivoAssinado);
			out.write(contentInfo.getDEREncoded());
			out.close();
			
			imprimirLog("Arquivo assinado gravado");
			
			return new SignedMessage(message, arquivoAssinado);
		}
		catch (OutOfMemoryError e) {
			// Se o arquivo for muito grande poderar acontecer um java heap space
			throw new Exception(e);
		}
	}
	
	private void imprimirLog(String msg) {
            if (log != null) {
                log.printLog(msg);
            }
	}

	private void gravaConfiguracaoes(Configuration repositorio) throws Exception {
		
		this.repository = repositorio;
		
		if (repositorio != null && repositorio.isDefinedType()) {
			
			Preferences preferences = getPreferences(SignConstants.PATH);

			preferences.clear();
			
			preferences.put(SignConstants.KEY_KEYSTORE_TYPE, repositorio.getKeyStoreType());
				
			if (repositorio.isDefinedPkcs12File()) {
				preferences.put(SignConstants.KEY_PKCS12_FILENAME, repositorio.getPkcs12File().getAbsolutePath());
			}

			preferences.sync();
		}
	}
		
	public SignedMessage signFile(File file, boolean attached) throws Exception {
				
		if (file == null) {
			throw new Exception("Por favor, selecione algum arquivo para realizar a assinatura digital!");
		}
	
		MessageToSign messageToSign = new MessageToSign(file.getName(), new FileInputStream(file));
		
			
		List<SignedMessage> signedData = signMessages(Arrays.asList(messageToSign), attached);
		
		return signedData.get(0);
	}
		
	public List<SignedMessage> signMessages(List<InputStream> messages, boolean attached) throws Exception {

		if (messages == null || messages.isEmpty()) {
			throw new Exception("Por favor, selecione alguma mensagem para realizar a assinatura digital!");
		}

		KeyStoreHelper storeHelper = initKeyStore();

		imprimirLogEhProgresso("Iniciando o processo de assinatura...");

		imprimirLog("O tipo de assinatura é \"" + (attached ? "atachada" : "detachada") + "\"");

		Certificate[] certsChain = storeHelper.getCertsChain();
	
		List<Certificate> certList = new ArrayList<Certificate>();

		imprimirLogEhProgresso("Carregando a cadeia de certificados...");
		
		if (certsChain != null && certsChain.length != 0) {
            certList.addAll(Arrays.asList(certsChain));
		}
		else {
			certList.add(storeHelper.getCertificate());
		}

		CertStore certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), BouncyCastleProvider.PROVIDER_NAME);
		
		imprimirLogEhProgresso("Iniciando a assinatura");
		
		int i=1;
		
		// Verifica se tem algum arquivo ja assinado
		for (InputStream message : messages) {

			byte[] mensagem = FileUtils.getInputStreamBytes(message);

			if (isSignedData(mensagem)) {
				throw new Exception (MessageFormat.format("O assinador não suporta co-assinatura! o arquivo ({0}) já foi assinado!", i));
			}
			
			i++;
		}
		
		List<SignedMessage> arquivosAssinados = new ArrayList<SignedMessage>();

		for (InputStream message : messages) {
			arquivosAssinados.add(signMessage(storeHelper, certs, attached, arquivo));
		}
		
		return arquivosAssinados;
	}
		
	public KeyStoreHelper initKeyStore() throws Exception {
		
		if (repository == null || !repository.isDefinedType()) {
			
			mostrarConfiguracao();

			if (repository == null || !repository.isDefinedType()) {
				throw new Exception("Por favor, para realizar a assinatura deve-se configurar o tipo de repositório!");
			}
		}
		
		if (repository.isTypePkcs12()) {

			if (!repository.isDefinedPkcs12File()) {
				throw new Exception("Por favor, para realizar a assinatura utilizando PKCS12, deve-se definir o endereço do arquivo do certificado!");
			}
			
			char[] pkcs12Senha;
			
			// Verifica se tem cache de senha do repositorio pkcs 12
			if (!repository.isDefinedPkcs12Password()) { 
							
				frmCertificadoPkcs12Senha.iniciar();

				if (frmCertificadoPkcs12Senha.getReturnStatus() == FrmCertificadoPkcs12Senha.RET_CANCEL) {
					throw new Exception("Por favor, para realizar a assinatura utilizando PKCS12, deve-se informar a senha do certificado!");
				}						
				
				pkcs12Senha = frmCertificadoPkcs12Senha.getSenha();				
			}
			else {
				pkcs12Senha = repository.getPkcs12Password();
			}
			
			PKCS12KeyStoreHelper storeHelperPkcs12 = new PKCS12KeyStoreHelper(new FileInputStream(
					repository.getPkcs12File()), 
					pkcs12Senha
			);
			
			repository.setPkcs12Password(pkcs12Senha);
			
			return storeHelperPkcs12;
		}
		else if (repository.isTypeMscapi()) {
			
			List<X509Certificate> certificados = MSCAPIKeyStoreHelper.getCertificatesAvailable();
						
			Collections.sort(certificados, new Comparator<X509Certificate>() {
                                @Override
				public int compare(X509Certificate item1, X509Certificate item2) {
					
					String alias1 = CertificateUtils.getCertificadoCN(item1.getSubjectDN().getName());
					String alias2 = CertificateUtils.getCertificadoCN(item2.getSubjectDN().getName());

					return alias1.compareToIgnoreCase(alias2);
				}
			});
						
			if (certificados.isEmpty()) {
				throw new Exception("Por favor, para realizar a assinatura utilizando Windows MsCAPI, deve-se cadastrar os certificados!");
			}
			else if (certificados.size() == 1) {					
				return new MSCAPIKeyStoreHelper(certificados.get(0));
			}
			else if (this.msCapiCertificado != null) {
				return new MSCAPIKeyStoreHelper(msCapiCertificado);				
			}
			else {
				frmSelecionarCertificadoMscapi.iniciar(certificados);

				if (frmSelecionarCertificadoMscapi.getReturnStatus() == FrmSelecionarCertificadoMscapi.RET_CANCEL) {
					throw new Exception("Por favor, para realizar a assinatura utilizando Windows MsCAPI, deve-se escolher algum certificado!");
				}

				this.msCapiCertificado = frmSelecionarCertificadoMscapi.getCertificado();
				return new MSCAPIKeyStoreHelper(msCapiCertificado);
			}
		}
		else {
			throw new Exception("Por favor, para realizar a assinatura deve-se configurar o tipo de repositório!");
		}
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
	
	public void mostrarConfiguracao() {
		
		dlgKeyStoreType.start(repository);
				
		if (dlgKeyStoreType.getReturnStatus() == FrmTipoRepositorio.RET_OK) {
			try {
				gravaConfiguracaoes(dlgKeyStoreType.getTipoRepositorio());
			}
			catch (Exception e) {
				JFrameUtils.showErro("Erro ao gravar as configurações!", e.getMessage(), null);
			}
		}
	}
}
