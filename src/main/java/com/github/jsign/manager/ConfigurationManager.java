package com.github.jsign.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.model.AvailableProvider;
import com.github.jsign.model.Configuration;
import com.github.jsign.model.MSCAPIAvailableProvider;
import com.github.jsign.model.PKCS11AvailableProvider;

public class ConfigurationManager {

	public static final String PREFERENCES_PATH = "com/github/jsign";
	public static final String KEY_KEYSTORE_TYPE = "keyStoreType";
	public static final String KEY_PKCS12_FILENAME = "pkcs12Filename";

	private PKCS11Manager pkcs11Manager;
	private MSCAPIManager mscapiManager;
	private PKCS12Manager pkcs12Manager;
	
	public static Configuration writeConfiguration(Configuration configuration) throws BackingStoreException {
						
		if (configuration != null && configuration.isDefinedType()) {
				
			Preferences preferences = getPreferences(PREFERENCES_PATH);

			preferences.clear();
				
			preferences.put(KEY_KEYSTORE_TYPE, configuration.getKeyStoreType());
					
			if (configuration.isDefinedPkcs12File()) {
				preferences.put(KEY_PKCS12_FILENAME, configuration.getPkcs12File().getAbsolutePath());
			}

			preferences.sync();
		}
		
		return configuration;
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
					
				String type = preferences.get(KEY_KEYSTORE_TYPE, "false");
					
				if (Configuration.KEY_STORE_TYPE_MSCAPI.equals(type) || Configuration.KEY_STORE_TYPE_PKCS11.equals(type) || Configuration.KEY_STORE_TYPE_PKCS12.equals(type)) {
					
					configuration.setType(type);

					File pkcs12File = new File(preferences.get(KEY_PKCS12_FILENAME, "false"));
					
					if (pkcs12File.isFile()) {													
						configuration.setPkcs12File(pkcs12File);
					}
				}
			}
			
			return configuration;
		}
		catch (Exception e) {
			throw new Exception ("Erro ao ler as configurações!");
		}
	}

	public List<AvailableProvider> getAvailableProviders() {
		
		List<AvailableProvider> availableProviders = new ArrayList<AvailableProvider>();
		
		AvailableProvider mscapiAvailableProvider = mscapiManager.getAvailableProvider();
		
		if (mscapiAvailableProvider != null) {
			availableProviders.add(mscapiAvailableProvider);
		}
			
		availableProviders.addAll(pkcs11Manager.getAvailableProviders());
		
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
			keyStoreHelpers.addAll(pkcs11Manager.getKeyStoreHelpders((PKCS11AvailableProvider) availableProvider));
		}
		
		return keyStoreHelpers;
	}	
}