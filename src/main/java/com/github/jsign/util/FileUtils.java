/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jsign.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

/**
 *
 * @author pablo-moreira
 */
public class FileUtils {
		
	public static byte[] getFileBytes(File arquivo) throws Exception {
		 
		FileInputStream fis = new FileInputStream(arquivo);

		return getInputStreamBytes(fis);
	}
		
	public static String getFilenameWithoutExtension(String arg) {		
		if (arg.contains(".")) {
			return arg.substring(0, arg.lastIndexOf("."));
		}
		else {
			return arg;
		}
	}
    
    public static void writeFile(File arq, byte[] bytes) throws IOException {    	
    	FileOutputStream os = new FileOutputStream(arq);
    	os.write(bytes);
    	os.close();    	
    }        

	public static byte[] getInputStreamBytes(InputStream inputStream) throws Exception {
	
		boolean done = false;

		int bytesread = 0;

		byte[] bytes = new byte[inputStream.available()];

		BufferedInputStream bis = new BufferedInputStream(inputStream);
			
		do {
			int b = bis.read();

			if (b == -1) {
				done = true;
				break;
			} 
			else {
				bytes[bytesread++] = (byte) b;
			}

		} while (!done);
		
		bis.close();
		
		return bytes;
	}
	
	 public static String getFileSizeDescription(Long size) {
	    	
		if(size <= 0) {
			return "0";
		}
		
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		
		return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];		
	}
}