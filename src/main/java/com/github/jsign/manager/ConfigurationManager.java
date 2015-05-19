package com.github.jsign.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.model.AvailableProvider;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.KeyStoreType;
import com.github.jsign.model.MSCAPIAvailableProvider;
import com.github.jsign.model.PKCS11AvailableProvider;
import com.github.jsign.model.PKCS12AvailableProvider;
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
	
	private PKCS11Manager pkcs11Manager;
	private MSCAPIManager mscapiManager;
	private PKCS12Manager pkcs12Manager;
	
	public void writeConfiguration(Configuration configuration) throws Exception {
	
		try {
			Preferences preferences = getPreferences(PREFERENCES_PATH);

			preferences.clear();

			if (configuration.isDefinedKeyStoreHelper()) {
							
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

	public List<AvailableProvider> getAvailableProviders(Configuration configuration) {
		
		List<AvailableProvider> availableProviders = new ArrayList<AvailableProvider>();
		
		AvailableProvider mscapiAvailableProvider = mscapiManager.getAvailableProvider();
		
		if (mscapiAvailableProvider != null) {
			availableProviders.add(mscapiAvailableProvider);
		}
		
		availableProviders.addAll(pkcs12Manager.getAvailableProviders(configuration));
		availableProviders.addAll(pkcs11Manager.getAvailableProviders(configuration));
		
		return availableProviders;
	}

	public void setPKCS11Manager(PKCS11Manager pkcs11Manager) {
		this.pkcs11Manager = pkcs11Manager;
	}

	public void setMSCAPIManager(MSCAPIManager mscapiManager) {
		this.mscapiManager = mscapiManager;		
	}

	public void setPKCS12Manager(PKCS12Manager pkcs12Manager) {
		this.pkcs12Manager = pkcs12Manager;		
	}

	public List<KeyStoreHelper> getKeyStoresHelpersAvailable(AvailableProvider availableProvider) throws Exception {
		
		ArrayList<KeyStoreHelper> keyStoreHelpers = new ArrayList<KeyStoreHelper>();
		
		if (availableProvider instanceof MSCAPIAvailableProvider) {
			keyStoreHelpers.addAll(mscapiManager.getKeyStoreHelpers((MSCAPIAvailableProvider) availableProvider));
		}
		else if (availableProvider instanceof PKCS11AvailableProvider) {
			keyStoreHelpers.addAll(pkcs11Manager.getKeyStoreHelpers((PKCS11AvailableProvider) availableProvider));
		}
		else if (availableProvider instanceof PKCS12AvailableProvider) {
			keyStoreHelpers.addAll(pkcs12Manager.getKeyStoreHelpers((PKCS12AvailableProvider) availableProvider));
		}
		
		return keyStoreHelpers;
	}	

	public void addPkcs12Certificate(Configuration configuration, File pkcs12Certificate) throws Exception {
		
		configuration.addPkcs12Certificate(pkcs12Certificate);
		
		writeConfiguration(configuration);
	}

	public void deletePkcs12Certificate(Configuration configuration, File pkcs12Certificate) throws Exception {		
		
		configuration.getPkcs12Certificates().remove(pkcs12Certificate);
		
		writeConfiguration(configuration);		
	}

	public void addPkcs11Driver(Configuration configuration, File pkcs11Driver) throws Exception {
		
		configuration.addPkcs11Driver(pkcs11Driver);
		
		writeConfiguration(configuration);
	}

	public void deletePkcs11Driver(Configuration configuration, File pkcs11Driver) throws Exception {
		
		configuration.getPkcs11Drivers().remove(pkcs11Driver);
		
		writeConfiguration(configuration);
	}
	
//	public AvailableProvider getAvailableProvider(List<AvailableProvider> availableProviders, Configuration configuration) {
//		
//		for (AvailableProvider ap : availableProviders) {
//			if (ap.getType() == configuration.getKeyStoreType()) {
//   				if (KeyStoreType.MSCAPI == configuration.getKeyStoreType()) {
//   					return ap;
//   	        	}
//   	        	else if (KeyStoreType.PKCS11 == configuration.getKeyStoreType()) {
//
//   	        		PKCS11AvailableProvider ap11 = (PKCS11AvailableProvider) ap;
//
//   	    			if (ap11.getTokenConfig().getToken().getName().equals(configuration.getPkcs11Name())
//   	    					&& ap11.getTokenConfig().getLibrary().equals(configuration.getPkcs11Library())   	    					
//   	    					&& (ap11.getSlot() != null && ap11.getSlot().equals(configuration.getPkcs11Slot())
//   	    						|| ap11.getSlot() == null && configuration.getPkcs11Slot() == null)) {
//   	    				
//   	    			}
//   	    		}
//   	    		else if (KeyStoreType.PKCS12 == configuration.getKeyStoreType()) {
//   	    			
//   	    			PKCS12AvailableProvider ap12 = (PKCS12AvailableProvider) ap;
//   	    			
//   	    			if (ap12.getPkcs12Certificate().getAbsoluteFile().equals(configuration.getPkcs12Filename())) {
//   	    				return ap12;
//   	    			}
//  	    		}
//   			}
//		}
//	}

	public KeyStoreHelper retrieveKeyStoreHelperByConfiguration(Configuration configuration) throws Exception {
		
		if (configuration.getKeyStoreType() != null) {	
			if (KeyStoreType.MSCAPI == configuration.getKeyStoreType()) {
				return mscapiManager.retrieveKeyStoreHelperByConfiguration(configuration);
			}
			else if (KeyStoreType.PKCS12 == configuration.getKeyStoreType()) {
				return pkcs12Manager.retrieveKeyStoreHelperByConfiguration(configuration);
			}
			else if (KeyStoreType.PKCS11 == configuration.getKeyStoreType()) {
				return pkcs11Manager.retrieveKeyStoreHelperByConfiguration(configuration);
			}
		}
		
		return null;
	}
}