package com.earq.getnet.qa;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

public class PDFQAImpl extends PDFViewer {

	public static void main(String[] args) {
		PDFQAImpl program = new PDFQAImpl();

		program.init(args);

	}

	public Map<String, File[]> getSuspectFiles(File file) {

		// TODO
		
//		
//		Thread t = new Thread();
//		try {
//			t.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		

		return getTeste(file);

		// return !"OS_4200428_1113.pdf".equals(file.getName());
		// return Math.random() < 0.5;
		// return false;
		// return true;

	}

	public void doArquivoValido(File file) {

		// todo
		System.out.println("Arquivo válido: " + file.getName());

	}

	public void doArquivoInvalido(File file) {

		// todo

		System.out.println("*Arquivo inválido: " + file.getName());

	}

	public Map<String, File[]> getTeste(File filesPath) {
		// File filesPath = new File(filePath);

		Map<String, File[]> files = new HashMap<String, File[]>();

		if (filesPath.isDirectory()) {

			FileFilter directoryFilter = new FileFilter() {
				public boolean accept(File file) {
					return file.isDirectory()
							&& file.getName().toLowerCase().contains("caixa");
				}
			};
			
			FileFilter PDFfile = new FileFilter() {
				public boolean accept(File file) {
					return file.getName().toLowerCase().endsWith(".pdf");
				}
			};

			File[] caixas = filesPath.listFiles(directoryFilter);
			
			for (int i = 0; i < caixas.length; i++) {
				
				File[] filesArray = caixas[i].listFiles(PDFfile); 
				
				files.put(caixas[i].getName(), filesArray);
				System.out.println("Cx: "+caixas[i].getName());
				
				for (int j = 0; j < filesArray.length; j++) {
					
					File f = filesArray[j];
					
					PDFViewer.currentFileProcessing = f;

				
				}
				
				
				
			}
		}
		
		
		return files;

	}

}
