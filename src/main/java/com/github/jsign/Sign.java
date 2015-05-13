package com.github.jsign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
import com.github.jsign.keystore.KeyStoreMscapiHelper;
import com.github.jsign.keystore.KeyStorePkcs12Helper;
import com.github.jsign.model.SignedFile;
import com.github.jsign.model.Repository;
import com.github.jsign.util.FileUtils;
import com.github.jsign.util.CertificateUtils;
import com.github.jsign.util.JFrameUtils;

public class Sign {

	private FrmCertificadoPkcs12Senha frmCertificadoPkcs12Senha;
	private FrmSelecionarCertificadoMscapi frmSelecionarCertificadoMscapi;
	private FrmTipoRepositorio frmTipoRepositorio;
	private Repository repository;
	private SignProgress progresso;
	private SignLog log;
	private X509Certificate msCapiCertificado;
	
	public Sign() throws Exception {
		try {						
			Security.addProvider(new BouncyCastleProvider());

			if (Security.getProvider(KeyStoreMscapiHelper.PROVIDER) == null) {
				System.out.println(MessageFormat.format("O provider {0} não esta instalado", KeyStoreMscapiHelper.PROVIDER));
			}		

			try {
				loadConfigurations();
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
			frmSelecionarCertificadoMscapi = new FrmSelecionarCertificadoMscapi(null, true);
			frmCertificadoPkcs12Senha = new FrmCertificadoPkcs12Senha(null, true);
			frmTipoRepositorio = new FrmTipoRepositorio(null, true);
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
					
				if (Repository.KEY_STORE_TYPE_MSCAPI.equals(type) || Repository.KEY_STORE_TYPE_PKCS12.equals(type)) {

					repository = new Repository();
					repository.setType(type);

					File pkcs12File = new File(preferences.get(SignConstants.KEY_PKCS12_FILENAME, "false"));
					
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

	private SignedFile assinarArquivo(KeyStoreHelper storeHelper, CertStore certs, boolean assinaturaAtachada, File arquivo) throws Exception {

		imprimirLogEhProgresso("Assinando: " + arquivo.getName());
		
		try {
			CMSProcessable msg = new CMSProcessableFile(arquivo);

			CMSSignedDataGenerator gen = new CMSSignedDataGenerator();                
			gen.addSigner(storeHelper.getPrivateKey(), storeHelper.getCertificate(), CMSSignedDataGenerator.DIGEST_SHA1);
			gen.addCertificatesAndCRLs(certs);

			CMSSignedData signedData = gen.generate(msg, assinaturaAtachada, storeHelper.getKeyStore().getProvider().getName());

			ContentInfo contentInfo = signedData.getContentInfo(); 
                
			File dirTmp = new File(System.getProperty("java.io.tmpdir"));

			File arquivoAssinado = new File(dirTmp, arquivo.getName() + ".p7s");

			imprimirLog("Gravando arquivo assinado: " + arquivoAssinado.getName());

			OutputStream out = new FileOutputStream(arquivoAssinado);
			out.write(contentInfo.getDEREncoded());
			out.close();
			
			imprimirLog("Arquivo assinado gravado");
			
			return new SignedFile(arquivo, arquivoAssinado);
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

	private void gravaConfiguracaoes(Repository repositorio) throws Exception {
		
		this.repository = repositorio;
		
		if (repositorio != null && repositorio.isDefinedType()) {
			
			Preferences preferences = getPreferences(SignConstants.PATH);

			preferences.clear();
			
			preferences.put(SignConstants.KEY_REPOSITORY_TYPE, repositorio.getType());
				
			if (repositorio.isDefinedPkcs12File()) {
				preferences.put(SignConstants.KEY_PKCS12_FILENAME, repositorio.getPkcs12File().getAbsolutePath());
			}

			preferences.sync();
		}
	}
		
	public SignedFile assinarArquivo(File arquivo, boolean assinaturaAtachada) throws Exception {
				
		if (arquivo == null) {
			throw new Exception("Por favor, selecione algum arquivo para realizar a assinatura digital!");
		}

		List<SignedFile> arquivosAssinados = assinarArquivos(Arrays.asList(arquivo), assinaturaAtachada);
		
		return arquivosAssinados.get(0);
	}
		
	public List<SignedFile> assinarArquivos(List<File> arquivos, boolean assinaturaAtachada) throws Exception {

		if (arquivos == null || arquivos.isEmpty()) {
			throw new Exception("Por favor, selecione algum arquivo para realizar a assinatura digital!");
		}
		
		KeyStoreHelper storeHelper = instanciarRepositorio();
				
		imprimirLogEhProgresso("Iniciando o processo de assinatura...");
				
		imprimirLog("O tipo de assinatura é \"" + (assinaturaAtachada ? "atachada" : "detachada") + "\"");
		
		Certificate[] signatarioCertificadoCadeia = storeHelper.getCertsChain();
	
		List<Certificate> certList = new ArrayList<Certificate>();

		imprimirLogEhProgresso("Carregando a cadeia de certificados...");
		
		if (signatarioCertificadoCadeia != null && signatarioCertificadoCadeia.length != 0) {
            certList.addAll(Arrays.asList(signatarioCertificadoCadeia));
		}
		else {
			certList.add(storeHelper.getCertificate());
		}

		CertStore certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), BouncyCastleProvider.PROVIDER_NAME);
		
		imprimirLogEhProgresso("Iniciando a assinatura");
		
		// Verifica se tem algum arquivo ja assinado
		for (File arquivo : arquivos) {

			byte[] mensagem = FileUtils.getArquivoBytes(arquivo);

			if (isAssinado(mensagem)) {
				throw new Exception (MessageFormat.format("O assinador não suporta co-assinatura! o arquivo ({0}) já foi assinado!", arquivo.getName()));
			}
		}
		
		List<SignedFile> arquivosAssinados = new ArrayList<SignedFile>();

		for (File arquivo : arquivos) {
			arquivosAssinados.add(assinarArquivo(storeHelper, certs, assinaturaAtachada, arquivo));
		}
		
		return arquivosAssinados;
	}
		
	public KeyStoreHelper instanciarRepositorio() throws Exception {
		
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
			
			KeyStorePkcs12Helper storeHelperPkcs12 = new KeyStorePkcs12Helper(new FileInputStream(
					repository.getPkcs12File()), 
					pkcs12Senha
			);
			
			repository.setPkcs12Password(pkcs12Senha);
			
			return storeHelperPkcs12;
		}
		else if (repository.isTypeMscapi()) {
			
			List<X509Certificate> certificados = KeyStoreMscapiHelper.getCertificatesAvailable();
						
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
				return new KeyStoreMscapiHelper(certificados.get(0));
			}
			else if (this.msCapiCertificado != null) {
				return new KeyStoreMscapiHelper(msCapiCertificado);				
			}
			else {
				frmSelecionarCertificadoMscapi.iniciar(certificados);

				if (frmSelecionarCertificadoMscapi.getReturnStatus() == FrmSelecionarCertificadoMscapi.RET_CANCEL) {
					throw new Exception("Por favor, para realizar a assinatura utilizando Windows MsCAPI, deve-se escolher algum certificado!");
				}

				this.msCapiCertificado = frmSelecionarCertificadoMscapi.getCertificado();
				return new KeyStoreMscapiHelper(msCapiCertificado);
			}
		}
		else {
			throw new Exception("Por favor, para realizar a assinatura deve-se configurar o tipo de repositório!");
		}
	}

	public boolean isAssinado(byte[] mensagem) {
		try {
			CMSSignedData pkcs7 = new CMSSignedData(mensagem);
			pkcs7.getSignedContent();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public void mostrarConfiguracao() {
		AccessController.doPrivileged(new PrivilegedAction<Void>() {			
			@Override
			public Void run() {

				frmTipoRepositorio.iniciar(repository);
				
				if (frmTipoRepositorio.getReturnStatus() == FrmTipoRepositorio.RET_OK) {
					try {
						gravaConfiguracaoes(frmTipoRepositorio.getTipoRepositorio());
					}
					catch (Exception e) {
						JFrameUtils.showErro("Erro ao gravar as configurações!", e.getMessage(), null);
					}
				}
				return null;
			}
		});
	}
}
