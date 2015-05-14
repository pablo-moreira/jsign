package com.github.jsign;

import java.io.File;

import junit.framework.TestCase;

import com.github.jsign.Sign;
import com.github.jsign.model.SignedMessage;
import com.github.jsign.util.FileUtils;


public class JSignTest extends TestCase {
    
    public static void testSignHugeFile() throws Exception {
    
        final Sign sign = new Sign();

        sign.showConfiguration();
        
        Runtime runtime = Runtime.getRuntime();

        System.out.println("Processadores:" + runtime.availableProcessors());
        System.out.println("Memoria em uso: " + FileUtils.getTamanhoFormatado(runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("Memoria livre: " + FileUtils.getTamanhoFormatado(runtime.freeMemory()));
        System.out.println("Total de memoria: " + FileUtils.getTamanhoFormatado(runtime.totalMemory()));
        System.out.println("Maximo de memoria: " + FileUtils.getTamanhoFormatado(runtime.maxMemory()));
        System.out.println("----------------------------------------------------");
        
        SignedMessage assinarArquivo = sign.signFile(new File("/home/pablo-moreira/Desktop/teste2.tar.gz"), true);
        
        FileUtils.copy(assinarArquivo.getSignedData(), new File("/home/pablo-moreira/Desktop/teste2.tar.gz.p7s"));
        
        System.out.println("arquivo assinado com sucesso!");
        
        System.out.println("Memoria em uso: " + FileUtils.getTamanhoFormatado(runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("Memoria livre: " + FileUtils.getTamanhoFormatado(runtime.freeMemory()));
        System.out.println("Total de memoria: " + FileUtils.getTamanhoFormatado(runtime.totalMemory()));
        System.out.println("Maximo de memoria: " + FileUtils.getTamanhoFormatado(runtime.maxMemory()));
        
        System.out.println("teste");
        
        assertEquals(1, 1);
    }
}
