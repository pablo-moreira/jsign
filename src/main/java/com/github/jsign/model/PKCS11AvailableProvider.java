package com.github.jsign.model;

import java.security.KeyStore;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;

import com.github.jsign.gui.DlgProtectionCallback;
import com.github.jsign.util.StringUtils;

public class PKCS11AvailableProvider extends AvailableProvider {

	private Long slot;
	private TokenConfig tokenConfig;
	private KeyStore keyStore;
	private Provider provider;
	private DlgProtectionCallback dlgProtectionCallback;
	private String tokenLabel;

	public PKCS11AvailableProvider(Provider provider, TokenConfig tokenConfig, Long slot, String tokenLabel) {
		this.provider = provider;
		this.tokenConfig = tokenConfig;
		this.slot = slot;
		this.tokenLabel = tokenLabel;
		this.dlgProtectionCallback = new DlgProtectionCallback(getType().name(), getDescription());
	}

	public PKCS11AvailableProvider(Provider provider, TokenConfig tokenConfig, Long slot, KeyStore keyStore) {
		this(provider, tokenConfig, slot, (String) null); 
		this.keyStore = keyStore;
	}

	public Long getSlot() {
		return slot;
	}

	public TokenConfig getTokenConfig() {
		return tokenConfig;
	}

	@Override
	public KeyStoreType getType() {
		return KeyStoreType.PKCS11;
	}

	@Override
	public String getDescription() {
		
		List<String> description = new ArrayList<String>();	
						
		if (tokenLabel != null) {
			description.add("Token: " + tokenLabel); 
		}
		
		if (tokenConfig != null) {
			description.add("Name: " + tokenConfig.getToken().getName());
			description.add("Library: " + tokenConfig.getLibrary());
		}
		
		if (slot != null) {
			description.add("Slot: " + slot);
		}
		
		return StringUtils.join(description.toArray(new String[]{}), ", ");
	}

	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;		
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public Provider getProvider() {
		return provider;
	}
	
	public DlgProtectionCallback getDlgProtectionCallback() {
		return dlgProtectionCallback;
	}
}