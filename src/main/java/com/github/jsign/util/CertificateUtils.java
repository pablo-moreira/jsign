package com.github.jsign.util;

public class CertificateUtils {

	public static String getCertificateCN(String certificate) {

		if (certificate != null) {

			String[] splits = certificate.split(",");
			String result = "";
			for (int i = 0; i < splits.length; i++) {
				String obj = splits[i].trim();
				if (obj.startsWith("CN=")) {
					result = obj.substring(obj.indexOf("CN=") + 3);
					break;
				}
			}
			return result;
		}
		return "";
	}
}