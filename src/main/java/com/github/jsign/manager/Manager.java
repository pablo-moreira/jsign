package com.github.jsign.manager;

import com.github.jsign.JSign;

public class Manager {

	private PKCS11Manager pkcs11Manager;
	private PKCS12Manager pkcs12Manager;
	private ConfigurationManager configurationManager;
	private MSCAPIManager mscapiManager;
	private SignManager signManager;
	private JSign jSign;
		
	public Manager(JSign jSign) {
		this.jSign = jSign;
		this.mscapiManager = new MSCAPIManager(this);
		this.pkcs11Manager = new PKCS11Manager(this);
		this.pkcs12Manager = new PKCS12Manager(this);
		this.signManager = new SignManager(this);		
		this.configurationManager = new ConfigurationManager(this);		
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

	public JSign getJSign() {
		return jSign;
	}
}
