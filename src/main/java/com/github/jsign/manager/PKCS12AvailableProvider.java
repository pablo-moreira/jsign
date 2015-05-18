/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jsign.manager;

import com.github.jsign.model.AvailableProvider;
import com.github.jsign.model.KeyStoreType;
import java.io.File;

/**
 *
 * @author pablo-teste
 */
public class PKCS12AvailableProvider extends AvailableProvider {

	private final File pkcs12Certificate;

	public PKCS12AvailableProvider(File pkcs12Certificate) {
		this.pkcs12Certificate = pkcs12Certificate;
	}
	
	@Override
	public String getType() {
		return KeyStoreType.PKCS12.name();
	}

	@Override
	public String getDescription() {
		
		String description = "";

		description += pkcs12Certificate.getAbsolutePath();
		
		return description;
	}

	public File getPkcs12Certificate() {
		return pkcs12Certificate;
	}	
}