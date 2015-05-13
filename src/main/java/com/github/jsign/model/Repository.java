package com.github.jsign.model;

import java.io.File;

import com.github.jsign.util.StringUtils;

public class Repository {

	public static final String KEY_STORE_TYPE_MSCAPI = "mscapi";
	public static final String KEY_STORE_TYPE_PKCS11 = "pkcs11";
	public static final String KEY_STORE_TYPE_PKCS12 = "pkcs12";	
	
	private String type;
	private File pkcs12File;
	private char[] pkcs12Password;
	
	public File getPkcs12File() {
		return pkcs12File;
	}
	
	public char[] getPkcs12Password() {
		return pkcs12Password;
	}

	public String getType() {
		return type;
	}

	public boolean isDefinedPkcs12File() {
		return getPkcs12File() != null;
	}

	public boolean isDefinedType() {
		return type != null && !type.isEmpty(); 
	}
	
	public boolean isTypeMscapi() { 
		return KEY_STORE_TYPE_MSCAPI.equals(getType());		
	}
	
	public boolean isTypePkcs12() {
		return KEY_STORE_TYPE_PKCS12.equals(getType());	
	}
	
	public void setPkcs12File(File pkcs12File) {
		this.pkcs12File = pkcs12File;
	}
	
	public void setType(String type) {
		if ((this.type == null && type != null) || (this.type != null && !this.type.equals(type))) {
			this.type = type;
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
