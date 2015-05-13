package com.github.jsign.util;

public class CertificateUtils {

	public static String getCertificadoCN(String certificado) {

		if (certificado != null) {

			String[] splits = certificado.split(",");
			String resultado = "";
			for (int i = 0; i < splits.length; i++) {
				String obj = splits[i].trim();
				if (obj.startsWith("CN=")) {
					resultado = obj.substring(obj.indexOf("CN=") + 3);
					break;
				}
			}
			return resultado;
		}
		return "";
	}
}
