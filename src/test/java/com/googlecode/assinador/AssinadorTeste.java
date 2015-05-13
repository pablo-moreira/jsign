/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.assinador;

import java.io.File;

import junit.framework.TestCase;

import com.github.jsign.Sign;
import com.github.jsign.model.SignedFile;
import com.github.jsign.util.FileUtils;

/**
 *
 * @author 205327
 */
public class AssinadorTeste extends TestCase {
    
    public static void testaAssinador() throws Exception {
    
        final Sign assinador = new Sign();

        assinador.mostrarConfiguracao();
        
        Runtime runtime = Runtime.getRuntime();

        System.out.println("Processadores:" + runtime.availableProcessors());
        System.out.println("Memoria em uso: " + FileUtils.getTamanhoFormatado(runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("Memoria livre: " + FileUtils.getTamanhoFormatado(runtime.freeMemory()));
        System.out.println("Total de memoria: " + FileUtils.getTamanhoFormatado(runtime.totalMemory()));
        System.out.println("Maximo de memoria: " + FileUtils.getTamanhoFormatado(runtime.maxMemory()));
        System.out.println("----------------------------------------------------");
        
        SignedFile assinarArquivo = assinador.assinarArquivo(new File("/home/pablo-moreira/Desktop/teste2.tar.gz"), true);
        
        FileUtils.copiar(assinarArquivo.getSignedFile(), new File("/home/pablo-moreira/Desktop/teste2.tar.gz.p7s"));
        
        System.out.println("arquivo assinado com sucesso!");
        
        System.out.println("Memoria em uso: " + FileUtils.getTamanhoFormatado(runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("Memoria livre: " + FileUtils.getTamanhoFormatado(runtime.freeMemory()));
        System.out.println("Total de memoria: " + FileUtils.getTamanhoFormatado(runtime.totalMemory()));
        System.out.println("Maximo de memoria: " + FileUtils.getTamanhoFormatado(runtime.maxMemory()));
        
        System.out.println("teste");
        
        assertEquals(1, 1);
    }
}
