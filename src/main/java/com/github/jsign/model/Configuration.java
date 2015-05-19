package com.github.jsign.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.github.jsign.keystore.KeyStoreHelper;
import com.github.jsign.keystore.PKCS11KeyStoreHelper;
import com.github.jsign.keystore.PKCS12KeyStoreHelper;

public class Configuration {
	
	private KeyStoreHelper keyStoreHelper;
	
	// PKCS 12
	private List<File> pkcs12Certificates = new ArrayList<File>(); 
	private File pkcs12Filename;	
	
	// PKCS 11
	private List<File> pkcs11Drivers = new ArrayList<File>();
	private String pkcs11Name;
	private String pkcs11Library;	
	private Long pkcs11Slot;		
	
	// Certificate selected
	private String certificateAlias;

	private KeyStoreType keyStoreType;	

	public List<File> getPkcs12Certificates() {
		return pkcs12Certificates;
	}

	public void setPkcs12Certificates(List<File> pkcs12Certificates) {
		this.pkcs12Certificates = pkcs12Certificates;
	}

	public List<File> getPkcs11Drivers() {
		return pkcs11Drivers;
	}

	public void setPkcs11Drivers(List<File> pkcs11Drivers) {
		this.pkcs11Drivers = pkcs11Drivers;
	}
	
	public boolean isDefinedPkcs12Certificates() {
		return getPkcs12Certificates() != null && getPkcs12Certificates().size() > 0;
	}
	
	public boolean isDefinedPkcs11Drivers() {
		return getPkcs11Drivers() != null && getPkcs11Drivers().size() > 0;
	}

	public void addPkcs12Certificate(File pkcs12Certificate) {
		getPkcs12Certificates().add(pkcs12Certificate);
	}

	public void addPkcs11Driver(File pkcs11Driver) {
		getPkcs11Drivers().add(pkcs11Driver);
	}

	public File getPkcs12Filename() {
		return pkcs12Filename;
	}

	public void setPkcs12Filename(File pkcs12Filename) {
		this.pkcs12Filename = pkcs12Filename;
	}

	public String getPkcs11Name() {
		return pkcs11Name;
	}

	public void setPkcs11Name(String pkcs11Name) {
		this.pkcs11Name = pkcs11Name;
	}

	public String getPkcs11Library() {
		return pkcs11Library;
	}

	public void setPkcs11Library(String pkcs11Library) {
		this.pkcs11Library = pkcs11Library;
	}

	public Long getPkcs11Slot() {
		return pkcs11Slot;
	}

	public void setPkcs11Slot(Long pkcs11Slot) {
		this.pkcs11Slot = pkcs11Slot;
	}

	public String getCertificateAlias() {
		return certificateAlias;
	}

	public void setCertificateAlias(String certificateAlias) {
		this.certificateAlias = certificateAlias;
	}
	
	public KeyStoreHelper getKeyStoreHelper() {
		return keyStoreHelper;
	}

	public void setKeyStoreHelper(KeyStoreHelper keyStoreHelper) {
		
		this.keyStoreHelper = keyStoreHelper;
		
		if (keyStoreHelper != null) {
			
			this.keyStoreType = keyStoreHelper.getType();
			this.certificateAlias = keyStoreHelper.getCertificateAlias();
			
			if (KeyStoreType.PKCS11 == keyStoreHelper.getType()) {
				PKCS11KeyStoreHelper ks = (PKCS11KeyStoreHelper) keyStoreHelper;
				this.pkcs11Library = ks.getTokenConfig().getLibrary();
				this.pkcs11Name = ks.getTokenConfig().getToken().getName();
				this.pkcs11Slot = ks.getSlot();
			}
			else if (KeyStoreType.PKCS12 == keyStoreHelper.getType()) {
				PKCS12KeyStoreHelper ks = (PKCS12KeyStoreHelper) keyStoreHelper;
				this.pkcs12Filename = ks.getPkcs12Filename();			
			}
		}		
	}

	public boolean isDefinedKeyStoreHelper() {
		return getKeyStoreHelper() != null;
	}

	public KeyStoreType getKeyStoreType() {
		return keyStoreType;
	}

	public void setKeyStoreType(KeyStoreType keyStoreType) {
		this.keyStoreType = keyStoreType;
	}
}