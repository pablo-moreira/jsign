/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jsign.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;

/**
 *
 * @author pablo-moreira
 */
public class FileUtils {
		
	public static byte[] getArquivoBytes(File arquivo) throws Exception {
		 
		boolean done = false;

		int bytesread = 0;

		byte[] bytes = new byte[(int) arquivo.length()];

		FileInputStream fis = new FileInputStream(arquivo);

		BufferedInputStream bis = new BufferedInputStream(fis);

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
	
	public static String getAbsolutePath(String url) {
		String path = url.substring(7, url.length());
		try {
			return URLDecoder.decode(path, "utf-8");
		} 
		catch (UnsupportedEncodingException e) {
			return path;
		}
	}
	
	public static String getUrl(File file) {
		return "file://" + file.getAbsolutePath().replace('\\', '/'); 
	}
	
	public static String getFilenameWithoutExtension(String arg) {		
		if (arg.contains(".")) {
			return arg.substring(0, arg.lastIndexOf("."));
		}
		else {
			return arg;
		}
	}
	
	public static String getExtensao(String arg) throws Exception {
		try {
			String extensao = arg.substring(arg.lastIndexOf(".") + 1 , arg.length());
			return extensao;
		}
		catch (Exception e) {
			throw new Exception("Erro ao obter a extensão do arquivo (" + arg + ")!");
		}
	}
	
	public static String getContentType(File arquivo) {	
		String contentType = URLConnection.getFileNameMap().getContentTypeFor(arquivo.getAbsolutePath());
		return (contentType != null && !contentType.equals("")) ? contentType : "application/octet-stream";
	}
	
    public static void copy(File from, File to) throws FileNotFoundException, IOException {
        
        FileInputStream in = new FileInputStream(from);
                
        FileOutputStream out = new FileOutputStream(to);
                
        byte[] buf = new byte[1024];
                
        int len;
                
        while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
        }
                
        in.close();
                
        out.close();
    }	
    
    public static String getTamanhoFormatado(Long tamanho) {
    	
		if(tamanho <= 0) {
			return "0";
		}
		
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		
		int digitGroups = (int) (Math.log10(tamanho)/Math.log10(1024));
		
		return new DecimalFormat("#,##0.#").format(tamanho/Math.pow(1024, digitGroups)) + " " + units[digitGroups];		
	}
    
    
    public static boolean deletarDiretorioRecursivamente(File dir) {
        
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deletarDiretorioRecursivamente(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        
        // The directory is now empty so delete it
        return dir.delete();
    }
	
    public static File getDiretorioTemporario() {
		return new File(System.getProperty("java.io.tmpdir"));
	}
    
    public static File getDiretorioUsuario() {
		return new File(System.getProperty("user.home"));
	}
    
    public static void gravarArquivo(File arq, byte[] bytes) throws IOException {    	
    	FileOutputStream os = new FileOutputStream(arq);
    	os.write(bytes);
    	os.close();    	
    }
    
    public static String gerarHash(byte[] arquivoBytes) throws Exception {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] md5 = md.digest(arquivoBytes);

            BigInteger hash = new BigInteger(1, md5);

            return hash.toString(16);
        }
        catch (Exception e) {
            throw new Exception("Erro ao gerar o hash do arquivo!");
        }
    }
    
    public static String gerarHash(File arquivo) throws Exception {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            InputStream is = new FileInputStream(arquivo);  

            byte[] buffer = new byte[8192];  

            int read;
            
            while( (read = is.read(buffer)) > 0) {  
                md.update(buffer, 0, read);  
            } 
            
            byte[] md5 = md.digest();  

            is.close();
            
            BigInteger hash = new BigInteger(1, md5);

            return hash.toString(16);
        }
        catch (Exception e) {
            throw new Exception("Erro ao gerar o hash do arquivo!");
        }
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
}