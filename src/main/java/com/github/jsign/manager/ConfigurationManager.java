package com.github.jsign.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.MSCAPIKeyStoreHelper;
import com.github.jsign.model.AvailableProvider;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.KeyStoreType;
import com.github.jsign.model.MSCAPIAvailableProvider;
import com.github.jsign.model.PKCS11AvailableProvider;
import com.github.jsign.model.PKCS12AvailableProvider;
import com.github.jsign.model.Token;
import com.github.jsign.util.StringUtils;

public class ConfigurationManager {

	public static final String PREFERENCES_PATH = "com/github/jsign";
	
	public static final String KEY_KEYSTORE_TYPE = "keyStoreType";
	public static final String KEY_CERTIFICATE_ALIAS = "certificateAlias";
	
	public static final String KEY_PKCS12_CERTIFICATES = "pkcs12Certificates";
	public static final String KEY_PKCS12_CERTIFICATE = "pkcs12Certificate_";
	private static final String KEY_PKCS12_FILENAME = "pkcs12Filename";
	
	public static final String KEY_PKCS11_DRIVERS = "pkcs11Drivers";
	public static final String KEY_PKCS11_DRIVER = "pkcs11Driver_";
	private static final String KEY_PKCS11_NAME = "pkcs11Name";
	private static final String KEY_PKCS11_LIBRARY = "pkcs11Library";
	private static final String KEY_PKCS11_SLOT = "pkcs11Slot";
	
	private Manager manager;
	
	public ConfigurationManager(Manager manager) {
		this.manager = manager;
	}	

	public Manager getManager() {
		return manager;
	}

	public Configuration clearConfiguration() {

		try {
			Preferences preferences = getPreferences(PREFERENCES_PATH);
			preferences.clear();
		} 
		catch (BackingStoreException e) {
			throw new RuntimeException("Erro ao limpar as configurações, mensagem interna: " + e.getMessage());
		}
		
		return new Configuration();
	}
	
	public void writeConfiguration(Configuration configuration) throws Exception {
	
		try {
			Preferences preferences = getPreferences(PREFERENCES_PATH);

			preferences.clear();

			if (configuration.isDefinedKeyStoreType()) {
							
				preferences.put(KEY_KEYSTORE_TYPE, configuration.getKeyStoreType().name());
				preferences.put(KEY_CERTIFICATE_ALIAS, configuration.getCertificateAlias());

				if (KeyStoreType.PKCS12 == configuration.getKeyStoreType()) {
					preferences.put(KEY_PKCS12_FILENAME, configuration.getPkcs12Filename().getAbsolutePath());
				}
				else if (KeyStoreType.PKCS11 == configuration.getKeyStoreType()) {
					preferences.put(KEY_PKCS11_NAME, configuration.getPkcs11Name());
					preferences.put(KEY_PKCS11_LIBRARY, configuration.getPkcs11Library());
					preferences.putLong(KEY_PKCS11_SLOT, configuration.getPkcs11Slot());
				}
			}

			if (configuration.isDefinedPkcs12Certificates()) {

				List<File> pkcs12Certificates = configuration.getPkcs12Certificates();

				preferences.putInt(KEY_PKCS12_CERTIFICATES, pkcs12Certificates.size());

				for (int i=0; i < pkcs12Certificates.size(); i++) {
					File pkcs12Certificate = pkcs12Certificates.get(i);
					preferences.put(KEY_PKCS12_CERTIFICATE + i, pkcs12Certificate.getAbsolutePath());					
				}							
			}

			if (configuration.isDefinedPkcs11Drivers()) {

				List<File> pkcs11Drivers = configuration.getPkcs11Drivers();

				preferences.putInt(KEY_PKCS11_DRIVERS, pkcs11Drivers.size());

				for (int i=0; i < pkcs11Drivers.size(); i++) {
					File pkcs11Driver = pkcs11Drivers.get(i);	
					preferences.put(KEY_PKCS11_DRIVER + i, pkcs11Driver.getAbsolutePath());
				}
			}

			preferences.sync();		
		}
		catch (Exception e) {
			throw new Exception("Erro ao persistir as configurações, mensagem interna: " + e.getMessage());
		}
	}
	
	private static Preferences getPreferences(String path) throws BackingStoreException {
		
		String[] pathItens = path.split("/");
		
		Preferences preferences = Preferences.userRoot();

		for (String pathItem : pathItens) {
			preferences = preferences.node(pathItem);
		}		
		return preferences;
	}
	
	public Configuration loadConfigurations() throws Exception {

		try {
			Configuration configuration = new Configuration();
			
			// Verifica se encontra as configuracoes salvas nas preferencias
			Preferences preferences = getPreferences(PREFERENCES_PATH);

			if (preferences != null && preferences.keys().length != 0) {
				
				String certificateAlias = preferences.get(KEY_CERTIFICATE_ALIAS, "");
				
				if (!StringUtils.isNullOrEmpty(certificateAlias)) {
					configuration.setCertificateAlias(certificateAlias);
				}
				
				String type = preferences.get(KEY_KEYSTORE_TYPE, "");
									
				if (KeyStoreType.MSCAPI.name().equals(type)) {					
					configuration.setKeyStoreType(KeyStoreType.MSCAPI);
				}
				else if (KeyStoreType.PKCS11.name().equals(type)) {
				
					configuration.setKeyStoreType(KeyStoreType.PKCS11);
				
					String pkcs11Name = preferences.get(KEY_PKCS11_NAME, "");
					
					if (!StringUtils.isNullOrEmpty(pkcs11Name)) {
						configuration.setPkcs11Name(pkcs11Name);
					}
					
					String pkcs11Library = preferences.get(KEY_PKCS11_LIBRARY, "");
					
					if (!StringUtils.isNullOrEmpty(pkcs11Library)) {
						configuration.setPkcs11Library(pkcs11Library);
					}

					long pkcs11Slot = preferences.getLong(KEY_PKCS11_SLOT, -100);

					if (pkcs11Slot != -100) {
						configuration.setPkcs11Slot(pkcs11Slot);
					}
				}
				else if (KeyStoreType.PKCS12.name().equals(type)) {
										
					configuration.setKeyStoreType(KeyStoreType.PKCS12);
					
					String pkcs12Filename = preferences.get(KEY_PKCS12_FILENAME, "");
									
					if (!StringUtils.isNullOrEmpty(pkcs12Filename)) {
						
						File pkcs12File = new File(pkcs12Filename);
						
						if (pkcs12File.isFile() && pkcs12File.exists()) {
							configuration.setPkcs12Filename(pkcs12File);
						}
					}
					
					configuration.setKeyStoreType(KeyStoreType.PKCS12);
				}
				
				int pkcs12Certificates = preferences.getInt(KEY_PKCS12_CERTIFICATES, 0);
				
				for (int i=0; i < pkcs12Certificates; i++) {
					
					String pkcs12Certificate = preferences.get(KEY_PKCS12_CERTIFICATE + i, "");
					
					if (!StringUtils.isNullOrEmpty(pkcs12Certificate)) {
						File pkcs12CertificateFile = new File(pkcs12Certificate);
						
						if (pkcs12CertificateFile.isFile() && pkcs12CertificateFile.exists()) {
							configuration.addPkcs12Certificate(pkcs12CertificateFile);
						}
					}
				}
				
				int pkcs11Drivers = preferences.getInt(KEY_PKCS11_DRIVERS, 0);

				for (int i=0; i < pkcs11Drivers; i++) {
					
					String pkcs12Driver = preferences.get(KEY_PKCS11_DRIVER + i, "");
					
					if (!StringUtils.isNullOrEmpty(pkcs12Driver)) {
						
						File pkcs12DriverFile = new File(pkcs12Driver);
						
						if (pkcs12DriverFile.isFile() && pkcs12DriverFile.exists()) {
							configuration.addPkcs11Driver(pkcs12DriverFile);
						}
					}
				}
			}
			
			return configuration;
		}
		catch (Exception e) {
			throw new Exception ("Erro ao ler as configurações!");
		}
	}

	public List<AvailableProvider> getAvailableProviders(Configuration configuration, boolean allowsPkcs12Certificate) {
		
		List<AvailableProvider> availableProviders = new ArrayList<AvailableProvider>();
		
		AvailableProvider mscapiAvailableProvider = getManager().getMscapiManager().getAvailableProvider();
		
		if (mscapiAvailableProvider != null) {
			availableProviders.add(mscapiAvailableProvider);
		}
		
		if (allowsPkcs12Certificate) {
			availableProviders.addAll(getManager().getPkcs12Manager().getAvailableProviders(configuration));			
		}
		
		availableProviders.addAll(getManager().getPkcs11Manager().getAvailableProviders(configuration));
		
		return availableProviders;
	}
	
	public List<KeyStoreHelper> getKeyStoresHelpersAvailable(AvailableProvider availableProvider) throws Exception {
		
		ArrayList<KeyStoreHelper> keyStoreHelpers = new ArrayList<KeyStoreHelper>();
		
		if (availableProvider instanceof MSCAPIAvailableProvider) {
			keyStoreHelpers.addAll(getManager().getMscapiManager().getKeyStoreHelpers((MSCAPIAvailableProvider) availableProvider));
		}
		else if (availableProvider instanceof PKCS11AvailableProvider) {
			keyStoreHelpers.addAll(getManager().getPkcs11Manager().getKeyStoreHelpers((PKCS11AvailableProvider) availableProvider));
		}
		else if (availableProvider instanceof PKCS12AvailableProvider) {
			keyStoreHelpers.addAll(getManager().getPkcs12Manager().getKeyStoreHelpers((PKCS12AvailableProvider) availableProvider));
		}
		
		return keyStoreHelpers;
	}	

	public KeyStoreHelper loadKeyStoreHelperByConfiguration(Configuration configuration) throws Exception {
		
		if (configuration.getKeyStoreType() != null) {	
			if (KeyStoreType.MSCAPI == configuration.getKeyStoreType()) {
				return getManager().getMscapiManager().retrieveKeyStoreHelperByConfiguration(configuration);
			}
			else if (KeyStoreType.PKCS12 == configuration.getKeyStoreType()) {
				return getManager().getPkcs12Manager().retrieveKeyStoreHelperByConfiguration(configuration);
			}
			else if (KeyStoreType.PKCS11 == configuration.getKeyStoreType()) {
				return getManager().getPkcs11Manager().retrieveKeyStoreHelperByConfiguration(configuration);
			}
		}
		
		return null;
	}

	public List<AvailableProvider> getAvailableProviders(Configuration configuration) {
		return getAvailableProviders(configuration, true);
	}

	public List<MSCAPIKeyStoreHelper> getKeyStoresHelpersAvailableOnMsCapi() {		
		return getManager().getMscapiManager().getKeyStoreHelpersAvailable();
	}

	public List<Token> getTokensDriversInstalledOnSystem(Configuration configuration) {
		return getManager().getPkcs11Manager().getTokensDriversInstalledOnSystem(configuration);
	}	
}