package com.earq.getnet.qa;

import java.io.File;

public class PDFQAImpl extends PDFViewer {

	public static void main(String[] args) {
		PDFQAImpl program = new PDFQAImpl();
		
		program.init(args);
		
	}

	public boolean validateFile(File file) {

		// todo

		
//		return !"OS_4200428_1113.pdf".equals(file.getName());
		return Math.random() < 0.5;
//		return false;
//		return true;

	}

	public void doArquivoValido(File file) {

		// todo
		System.out.println("Arquivo válido: " + file.getName());

	}

	public void doArquivoInvalido(File file) {

		// todo

		System.out.println("*Arquivo inválido: " + file.getName());

	}

}
