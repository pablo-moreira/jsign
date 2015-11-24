package com.github.jsign.util;


public abstract class CertificateFinder implements Runnable {
	
	private boolean stop;
	
	public boolean isStop() {
		return stop;
	}

	public void stop() {
		stop = true;
	}	
}
