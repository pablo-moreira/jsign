package com.github.jsign.model;

import java.io.File;

import com.github.jsign.util.StringUtils;

public class Configuration {

	public static final String KEY_STORE_TYPE_MSCAPI = "MSCAPI";
	public static final String KEY_STORE_TYPE_PKCS11 = "PKCS11";
	public static final String KEY_STORE_TYPE_PKCS12 = "PKCS12";	
	
	private String keyStoreType;
	private File pkcs12File;
	private char[] pkcs12Password;
	
	public File getPkcs12File() {
		return pkcs12File;
	}
	
	public char[] getPkcs12Password() {
		return pkcs12Password;
	}

	public String getKeyStoreType() {
		return keyStoreType;
	}

	public boolean isDefinedPkcs12File() {
		return getPkcs12File() != null;
	}

	public boolean isDefinedType() {
		return keyStoreType != null && !keyStoreType.isEmpty(); 
	}
	
	public boolean isTypeMscapi() { 
		return KEY_STORE_TYPE_MSCAPI.equals(getKeyStoreType());		
	}
	
	public boolean isTypePkcs12() {
		return KEY_STORE_TYPE_PKCS12.equals(getKeyStoreType());	
	}
	
	public void setPkcs12File(File pkcs12File) {
		this.pkcs12File = pkcs12File;
	}
	
	public void setType(String type) {
		if ((this.keyStoreType == null && type != null) || (this.keyStoreType != null && !this.keyStoreType.equals(type))) {
			this.keyStoreType = type;
			setPkcs12Password(null);
		}
	}
	
	public void setPkcs12Password(char[] pkcs12Senha) {
		this.pkcs12Password = pkcs12Senha;
	}	
	
	public boolean isDefinedPkcs12Password() {
		return !StringUtils.isNullOrEmpty(getPkcs12Password());
	}
}