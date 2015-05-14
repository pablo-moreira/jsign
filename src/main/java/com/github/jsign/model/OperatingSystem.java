package com.github.jsign.model;

public enum OperatingSystem {
		
	LINUX("Linux"),
	WINDOWS("Windows"), 
	MACOS("Macos");
	
	private final String name;
	
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

	public static boolean isLinux() {
		return getOsName().startsWith(LINUX.getName());
	}

	public static boolean isWindows() {
		return getOsName().startsWith(WINDOWS.getName());
	}

	public static OperatingSystem getOperatingSystem() {
		if (isWindows()) {
			return WINDOWS;
		}
		else if (isLinux()) {
			return LINUX;
		}
		else {
			return MACOS;
		}		
	}
}