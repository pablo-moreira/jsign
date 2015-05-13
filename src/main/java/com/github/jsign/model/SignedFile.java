package com.github.jsign.model;

import java.io.File;

public class SignedFile {

	private File signedFile;
	private File originalFile;

	public SignedFile(File originalFile, File signedFile) {
		this.signedFile = signedFile;
		this.originalFile = originalFile;
	}

	public File getSignedFile() {
		return signedFile;
	}

	public File getOriginalFile() {
		return originalFile;
	}
}
