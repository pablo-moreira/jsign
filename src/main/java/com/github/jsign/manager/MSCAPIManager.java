package com.github.jsign.manager;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.MSCAPIKeyStoreHelper;
import com.github.jsign.model.OperatingSystem;

public class MSCAPIManager {

	public boolean isAvailable() {		
		return OperatingSystem.isWindows() && Security.getProvider(MSCAPIKeyStoreHelper.PROVIDER) != null; 
	}
		
	public List<MSCAPIKeyStoreHelper> tryGetKeyStoreHelpersAvailable() {
		
		if (isAvailable()) {
			try {
				KeyStore keyStore = MSCAPIKeyStoreHelper.newKeyStoreInstance();
				
				List<X509Certificate> certificatesAvailable = KeyStoreHelper.getCertificatesAvailable(keyStore);
				
				List<MSCAPIKeyStoreHelper> helpers = new ArrayList<MSCAPIKeyStoreHelper>();
				
				for (X509Certificate certificate : certificatesAvailable) {					
					try {
						helpers.add(new MSCAPIKeyStoreHelper(keyStore, certificate));
					} 
					catch (Exception e) {
						// Skip certificate
					}
				}
				
				return helpers;
			}
			catch (Exception e) {}
		}

		return new ArrayList<MSCAPIKeyStoreHelper>();
	}
}