package com.github.jsign.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import com.github.jsign.model.OperatingSystem;
import com.github.jsign.model.Pkcs11Tokens;
import com.github.jsign.model.TokenConfig;

public class Pkcs11Utils {
	
	public static Provider getProvider(String name, String library, Integer slot) {
		
		Properties map = new Properties();
		
		map.setProperty("name", name);
		map.setProperty("library", library);
		
		if (slot != null) {
			map.setProperty("slot", slot.toString());
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try{
			map.store(bos, null);
			
			InputStream bin = new ByteArrayInputStream(bos.toByteArray());
			
			bos.close();
			
			Provider provider = new sun.security.pkcs11.SunPKCS11(bin);
			
			bin.close();
			
			return provider;
		}
		catch (Exception e) {
			throw new RuntimeException(MessageFormat.format("Erro ao instanciar o provider do token {0}, mensagem interna: {1}", name, e.getMessage()));
		}
	}

	public static void getKeyStore(TokenConfig tokenConfig) {

		File library = new File(tokenConfig.getLibrary());
		
		if (library.exists()) {
			
			try {
				Provider provider = getProvider(tokenConfig.getToken().getName(), tokenConfig.getLibrary(), tokenConfig.getSlot());
					
				Security.addProvider(provider);
				//fixAliases(provider);
				//KeyStoreHelper ksh = getKeyStoreHelper(map.getProperty("name"), map.getProperty("library"), provider);
				//return ksh;
			}
			catch(ProviderException e){
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		
		List<TokenConfig> tokensConfigsBySO = Pkcs11Tokens.getInstance().getTokensConfigsBySO(OperatingSystem.WINDOWS);
		
		for (TokenConfig tokenConfig : tokensConfigsBySO) {
			getKeyStore(tokenConfig);
		}
	}
}
