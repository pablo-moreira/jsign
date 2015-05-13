package com.github.jsign.model;

public enum OperatingSystem {
		
	LINUX("Linux"),
	WINDOWS("Windows"), 
	MACOS("Macos");
	
	private String name;
	
	private OperatingSystem(String name) {
		this.name = name;	
	}
	
	public String getName() {
		return name;
	}

	public static String getOsArch() {
		return System.getProperty("os.arch");
	}

	public static String getOsName() {
		return System.getProperty("os.name");
	}

	public static boolean isOsLinux() {
		return getOsName().startsWith(LINUX.getName());
	}

	public static boolean isOsWindows() {
		return getOsName().startsWith(WINDOWS.getName());
	}
}