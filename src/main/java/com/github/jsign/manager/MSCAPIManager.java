package com.github.jsign.manager;

import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.MSCAPIKeyStoreHelper;
import com.github.jsign.model.AvailableProvider;
import com.github.jsign.model.MSCAPIAvailableProvider;
import com.github.jsign.model.OperatingSystem;

public class MSCAPIManager {

	public List<MSCAPIKeyStoreHelper> getKeyStoreHelpers(MSCAPIAvailableProvider availableProvider) {
		
		List<MSCAPIKeyStoreHelper> helpers = new ArrayList<MSCAPIKeyStoreHelper>();
		
		try {
			List<X509Certificate> certificatesAvailable = KeyStoreHelper.getCertificatesAvailable(availableProvider.getKeyStore());
				
			for (X509Certificate certificate : certificatesAvailable) {					
				try {
					helpers.add(new MSCAPIKeyStoreHelper(availableProvider.getKeyStore(), certificate));
				}
				catch (Exception e) {
					// Skip certificate
				}
			}
				
			return helpers;
		}
		catch (Exception e) {
			return helpers;
		}
	}
	
	public AvailableProvider getAvailableProvider() {
		
		if (OperatingSystem.isWindows()) {
			
			Provider provider = Security.getProvider(MSCAPIKeyStoreHelper.PROVIDER);
			
			if (provider != null) {
			
				try {
					KeyStore keyStore = MSCAPIKeyStoreHelper.getNewKeyStore();
					
					List<X509Certificate> certificatesAvailable = KeyStoreHelper.getCertificatesAvailable(keyStore);
					
					if (certificatesAvailable.size() > 0) {
						return new MSCAPIAvailableProvider(provider, keyStore, certificatesAvailable);
					}
				}
				catch (Exception e) {}
			}
		}
		
		return null;
	}
}