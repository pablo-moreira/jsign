/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jsign.model;

import com.github.jsign.gui.DlgProtectionCallback;

import java.io.File;

/**
 *
 * @author pablo-teste
 */
public class PKCS12AvailableProvider extends AvailableProvider {

	private final File pkcs12Certificate;
	private final DlgProtectionCallback dlgProtectionCallback = new DlgProtectionCallback();

	public PKCS12AvailableProvider(File pkcs12Certificate) {
		this.pkcs12Certificate = pkcs12Certificate;
	}
	
	@Override
	public KeyStoreType getType() {
		return KeyStoreType.PKCS12;
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

	public DlgProtectionCallback getDlgProtectionCallback() {
		return dlgProtectionCallback;
	}	
}