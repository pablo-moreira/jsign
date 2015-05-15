package com.github.jsign.model;

import java.util.ArrayList;
import java.util.List;

public class PKCS11Tokens {

	private List<Token> tokens = new ArrayList<Token>();
		
	public PKCS11Tokens() {
		newToken("eTokfen")
			.addLibLinux("/lib/libeToken.so.8")
			.addLibLinux("/lib/libeToken.so.8.0")
			.addLibLinux("/lib64/libeToken.so.8")
			.addLibLinux("/lib64/libeToken.so.8.0")
			.addLibLinux("libeTPkcs11.so")
			.addLibLinux("libeToken.so")
			.addLibMacOS("/Library/Frameworks/eToken.framework/Versions/4.55.41/libeToken.dylib")
			.addLibMacOS("/usr/local/lib/libeTPkcs11.dylib")
			.addLibMacOS("/Library/Frameworks/eToken.framework/Versions/Current/libeToken.dylib")
			.addLibWindows("c:/windows/system32/eTPKCS11.dll")
			.addLibWindows("eTPKCS11.dll");
		
		newToken("OpenSC")
			.addLibLinux("/lib/opensc-pkcs11.so")
			.addLibLinux("/usr/lib/pkcs11/opensc-pkcs11.so")
			.addLibLinux("/usr/lib/opensc-pkcs11.so")
			.addLibWindows("c:/windows/system32/opensc-pkcs11.dll");				
		
		newToken("AET")
			.addLibWindows("aetpkcss1.dll")
			.addLibWindows("c:/windows/system32/aetpkss1.dll");
		
		newToken("AETUNX")
			.addLibLinux("/lib/libaetpkss.so")
			.addLibLinux("libaetpkss.so");
		
		newToken("GCLIB").addLibWindows("gclib.dll");
		newToken("PK2PRIV").addLibWindows("pk2priv.dll");
		newToken("W32PK2IG").addLibWindows("w32pk2ig.dll");
		newToken("NGP11V211").addLibWindows("ngp11v211.dll");
		newToken("ACOSPKCS11").addLibWindows("ngp11v211.dll");
		newToken("DKCK201").addLibWindows("dkck201.dll");
		newToken("DKCK232").addLibWindows("dkck232.dll");
		newToken("CRYPTOKI22").addLibWindows("cryptoki22.dll");
		newToken("ACPKCS").addLibWindows("acpkcs.dll");
		newToken("SLBCK").addLibWindows("slbck.dll");
		
		newToken("CMP11")
			.addLibLinux("libcmP11.so")
			.addLibLinux("/lib/libcmP11.so")
			.addLibWindows("cmP11.dll");

		newToken("WDPKCS")
			.addLibWindows("WDPKCS.dll")
			.addLibLinux("libwdpkcs.so")			
			.addLibLinux("/lib/libwdpkcs.so")
			.addLibLinux("/usr/local/lib64/libwdpkcs.so")
			.addLibLinux("/usr/local/lib/libwdpkcs.so")
			.addLibLinux("/usr/lib/watchdata/lib/libwdpkcs.so")
			.addLibLinux("/opt/watchdata/lib64/libwdpkcs.so")
			.addLibMacOS("libwdpkcs.dylib")
			.addLibMacOS("/usr/local/lib/libwdpkcs.dylib")
			.addLibWindows("C:/Windows/System32/Watchdata/Watchdata Brazil CSP v1.0/WDPKCS.dll");

		newToken("GPKCS11")
			.addLibLinux("libgpkcs11.so")
			.addLibLinux("libgpkcs11.so.2");

		newToken("EPSNG")
			.addLibLinux("libepsng_p11.so")
			.addLibLinux("libepsng_p11.so.1")
			.addLibLinux("/usr/local/ngsrv/libepsng_p11.so.1");
	}
	
	private Token newToken(String name) {
		Token token = new Token(name);
		tokens.add(token);
		return token;
	}

	public List<Token> getTokens() {
		return tokens;
	}
	
	public List<TokenConfig> getTokensConfigsByOperatingSystem(OperatingSystem operatingSystem) {
		
		List<TokenConfig> tokensConfigs = new ArrayList<TokenConfig>();
		
		for (Token token : getTokens()) {
			tokensConfigs.addAll(token.getConfigsByOperatingSystem(operatingSystem));
		}

		return tokensConfigs;
	}
}