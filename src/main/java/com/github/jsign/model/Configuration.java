package com.github.jsign.model;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
	
	private KeyStoreType keyStoreType;
	
	// PKCS 12
	private List<File> pkcs12Certificates = new ArrayList<File>(); 
	private String pkcs12Filename;	
	
	// PKCS 11
	private List<File> pkcs11Drivers = new ArrayList<File>();
	private String pkcs11Name;
	private String pkcs11Library;	
	private Integer pkcs11Slot;		
	
	// Certificate selected
	private String certificateAlias;	

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
	
	public KeyStoreType getKeyStoreType() {
		return keyStoreType;
	}

	public boolean isDefinedType() {
		return keyStoreType != null; 
	}
	
	public boolean isTypeMSCAPI() { 
		return getKeyStoreType() == KeyStoreType.MSCAPI;		
	}

	public boolean isTypePKCS11() {
		return getKeyStoreType() == KeyStoreType.PKCS11;
	}
	
	public boolean isTypePKCS12() {
		return getKeyStoreType() == KeyStoreType.PKCS12;
	}
		
	public void setType(KeyStoreType keyStoreType) {
		this.keyStoreType = keyStoreType;
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

	public String getPkcs12Filename() {
		return pkcs12Filename;
	}

	public void setPkcs12Filename(String pkcs12Filename) {
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

	public Integer getPkcs11Slot() {
		return pkcs11Slot;
	}

	public void setPkcs11Slot(Integer pkcs11Slot) {
		this.pkcs11Slot = pkcs11Slot;
	}

	public String getCertificateAlias() {
		return certificateAlias;
	}

	public void setCertificateAlias(String certificateAlias) {
		this.certificateAlias = certificateAlias;
	}	
}