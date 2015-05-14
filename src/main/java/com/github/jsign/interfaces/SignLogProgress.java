package com.github.jsign.interfaces;

public interface SignLogProgress extends SignLog, SignProgress  {

	public void printLogAndProgress(String msg);
	
}
