package com.github.jsign.manager;

public class Manager {

	private PKCS11Manager pkcs11Manager;
	private PKCS12Manager pkcs12Manager;
	private ConfigurationManager configurationManager;
	private MSCAPIManager mscapiManager;
	private SignManager signManager;
		
	public Manager() {
		mscapiManager = new MSCAPIManager(this);
		pkcs11Manager = new PKCS11Manager(this);
		pkcs12Manager = new PKCS12Manager(this);
		signManager = new SignManager(this);		
		configurationManager = new ConfigurationManager(this);		
	}

	public PKCS11Manager getPkcs11Manager() {
		return pkcs11Manager;
	}

	public PKCS12Manager getPkcs12Manager() {
		return pkcs12Manager;
	}

	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public MSCAPIManager getMscapiManager() {
		return mscapiManager;
	}

	public SignManager getSignManager() {
		return signManager;
	}
}
