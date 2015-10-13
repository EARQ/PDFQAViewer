package com.earq.getnet.qa;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;
import org.icepdf.ri.util.PropertiesManager;

public abstract class PDFViewer {

	private Map<String, File[]> files;
	private Object[] boxes;
	private File[] boxFiles;
	private int indexFile = -1;
	private int indexBox = 0;
	private JLabel labelFileName;
	private JLabel labelBoxName;
	private static JLabel fileProcessing;
	public static File currentFileProcessing;

	private JButton buttonInvalido;
	private JButton buttonValido;
	private JButton buttonShowXML;
	private JButton buttonStart;
	private JFileChooser fileChooser;
	private SwingController controller;
	private JFrame window;
	private XMLViewer xmlViewer;
	public static String DEFAULT_FOLDER = "C:\\TEMP\\GetNetAmostragem\\Semana x";
	public static int DEFAULT_TIMER_NEXT_WAIT = 1000;

	Map<Object, Integer> validator = new HashMap<Object, Integer>();
	Map<Object, Integer> validatorInvalid = new HashMap<Object, Integer>();

	public abstract Map<String, File[]> getSuspectFiles(File file);

	public abstract void doArquivoValido(File file);

	public abstract void doArquivoInvalido(File file);

	public void init(String[] args) {
		try {
			if (args.length > 0) {

				chooseFolder(args[0]);

			} else {
				chooseFolder();
			}

		} catch (Exception e) {
			terminate();
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void chooseFolder() throws Exception {
		chooseFolder(PDFViewer.DEFAULT_FOLDER);
	}

	public void chooseFolder(String defaultFolder) throws Exception {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		fileChooser = new JFileChooser();

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setSelectedFile(new File(defaultFolder));

		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			System.out
					.println("Lendo semana: " + fileChooser.getSelectedFile());

			showPanel();
		}

	}

	public void showPanel() {

		JPanel header = new JPanel();

		JLabel labelFolder = new JLabel(
				getLabelFolder(fileChooser.getSelectedFile()));

		fileProcessing = new JLabel(getLabelFileProcessing());
		
		fileProcessing.setVisible(true);

		labelFileName = new JLabel();
		labelBoxName = new JLabel();
		// labelEC = new JLabel(getLabelEC(file));

		header.add(labelFolder, BorderLayout.NORTH);
		header.add(labelFileName);
		header.add(labelBoxName);

		controller = new SwingController();

		PropertiesManager properties = new PropertiesManager(
				System.getProperties(),
				ResourceBundle
						.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));

		properties.set(PropertiesManager.PROPERTY_DEFAULT_ZOOM_LEVEL, "3");

		SwingViewBuilder factory = new SwingViewBuilder(controller, properties);

		JPanel viewerComponentPanel = factory.buildViewerPanel();
		ComponentKeyBinding.install(controller, viewerComponentPanel);

		controller.getDocumentViewController().setAnnotationCallback(
				new org.icepdf.ri.common.MyAnnotationCallback(controller
						.getDocumentViewController()));

		// JFileChooser fc = new JFileChooser();
		controller.setIsEmbeddedComponent(true);

		window = new JFrame("eBravo Digital QA");

		JFrame.setDefaultLookAndFeelDecorated(true);

		ImageIcon img = new ImageIcon("favicon.ico");

		window.setIconImage(img.getImage());

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window.add(header, BorderLayout.PAGE_START);

		JPanel buttons = new JPanel();

		buttonStart = new JButton(
				"<html><h1><font color='green'>&nbsp;&nbsp;Iniciar Processamento&nbsp;&nbsp;</font></h1>");

		buttonStart.addActionListener(new ActionListener() {

			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed

				buttonStart.setVisible(false);
				fileProcessing.setVisible(true);
				window.repaint();
				
				
				Timer timer = new Timer(200, new ActionListener() {
					public void actionPerformed(ActionEvent evt) {

						try {
							setFileProcessing();

						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}

				});
				timer.setRepeats(true);

				timer.start();

				files = getSuspectFiles(fileChooser.getSelectedFile());

				timer.stop();

				boxes = files.keySet().toArray();

				if (boxes.length == 0) {
					JOptionPane.showMessageDialog(null,
							"Nenhuma caixa encontrada.");
					return;
				}

				boxFiles = files.get(boxes[indexBox]);

				nextFile();

				window.remove(fileProcessing);
				window.add(viewerComponentPanel, BorderLayout.CENTER);
				window.repaint();

				viewerComponentPanel.setVisible(true);
				buttonValido.setVisible(true);
				buttonInvalido.setVisible(true);
				buttonShowXML.setVisible(true);

			}
		});

		buttons.add(buttonStart, BorderLayout.CENTER);

		buttonInvalido = new JButton(
				"<html><h1><font color='red'>Inválido</font></h1>");
		buttonInvalido.setVisible(false);

		buttonInvalido.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doArquivoInvalido(boxFiles[indexFile]);
				addInvalid(boxFiles[indexFile]);
				nextFile();
				disableButtons();
			}
		});

		buttons.add(buttonInvalido, BorderLayout.CENTER);

		buttonValido = new JButton(
				"<html><h1><font color='green'>&nbsp;&nbsp;Válido&nbsp;&nbsp;</font></h1>");

		buttonValido.setVisible(false);

		buttonValido.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doArquivoValido(boxFiles[indexFile]);
				nextFile();
				disableButtons();

			}
		});

		buttons.add(buttonValido, BorderLayout.CENTER);

		buttonShowXML = new JButton(
				"<html><h1><font color='blue'>&nbsp;&nbsp;&nbsp;XML&nbsp;&nbsp;&nbsp;</font></h1>");
		buttonShowXML.setVisible(false);

		buttonShowXML.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {

					if (xmlViewer == null) {
						xmlViewer = new XMLViewer(window);
					}

					String fileName = boxFiles[indexFile].getName();
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
					fileName += ".xml";

					xmlViewer.viewXML(boxFiles[indexFile].getParent()
							+ File.separator + fileName);

				} catch (Exception e1) {
					System.out.println("Erro ao abrir o XML: "
							+ e1.getMessage());
				}
			}
		});

		buttons.add(buttonShowXML, BorderLayout.CENTER);

		window.add(buttons, BorderLayout.SOUTH);

//		JPanel filePanel = new JPanel();
//
//		// filePanel.add(labelFileName);
//		// filePanel.add(labelBoxName);
//		// filePanel.add(labelEC);
//		// filePanel.add(viewerComponentPanel);
//		// filePanel.setEnabled(false);
//
//		window.add(filePanel, BorderLayout.AFTER_LINE_ENDS);

		window.add(fileProcessing, BorderLayout.CENTER);
		fileProcessing.setVisible(false);

		window.pack();
		window.setExtendedState(window.MAXIMIZED_BOTH);
		window.setVisible(true);

		// controller.openDocument(files[indexFile].getAbsolutePath());
		delayButtons();

	}

	protected void nextFile() {
		delayButtons();

		
		
		if (indexFile == boxFiles.length - 1) {
			if (indexBox == boxes.length - 1) {
				terminate();
				if (window != null) {
					window.dispose();
				}
				JOptionPane.showMessageDialog(null, "Validação finalizada");
				System.exit(0);
				return;
			}
			indexFile = 0;
			// files = boxe boxes[++indexBox].listFiles();

			boxFiles = files.get(boxes[++indexBox]);

		} else {
			indexFile++;
		}

		File next = boxFiles[indexFile];
		addSuspect(boxes[indexBox]);
		labelFileName.setText(getLabelFileName(next));
		labelBoxName.setText(getLabelBoxName(boxes[indexBox]));
		// labelEC.setText(getLabelEC(next));

		controller.openDocument(next.getAbsolutePath());
		controller.setPageViewMode(DocumentViewControllerImpl.ONE_PAGE_VIEW,
				true);


		delayButtons();

	}

	private String getLabelFileName(File file) {

		String unpadded = file.getName();
		String padded = "";
		String mask = "_____________________________";

		if (file.getName().length() <= mask.length()) {
			padded = mask.substring(unpadded.length());
		}

		return "<html><br/><h3>Arquivo:&nbsp;<font color='blue'>"
				+ file.getName() + "</font><font color='white'>" + padded
				+ "</font>&nbsp;&nbsp;&nbsp;</h3> ";
	}

	private String getLabelBoxName(Object caixa) {
		return "<html><br/><h3>Caixa:&nbsp;<font color='blue'>"
				+ caixa
				+ "</font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</h3> ";
	}

	private static String getLabelFileProcessing() {
		
		String fileName = "";
	
		
		return "<html><br/><h1>&nbsp;&nbsp;<font color='blue'>"
				+ "&nbsp;&nbsp;Processando Arquivos.....<br/><br/>"+"<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;"+fileName
				+ "</font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</h1> ";
	}

	// private String getLabelEC(File file) {
	// return "<html><br/><h2>EC:&nbsp;<font color='blue'>" + "TODO"
	// + "</font>&nbsp;&nbsp;&nbsp;</h2>";
	// }

	private String getLabelFolder(File file) {
		return "<html><br/><h3>Lendo Diretório:&nbsp;&nbsp;&nbsp;&nbsp;<font color='blue'>"
				+ file.getAbsolutePath() + "</font>&nbsp;&nbsp;&nbsp;</h3>";
	}

	private void delayButtons() {

		Timer timer = new Timer(DEFAULT_TIMER_NEXT_WAIT, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				buttonInvalido.setEnabled(true);
				buttonValido.setEnabled(true);
			}

		});
		timer.setRepeats(false);

		timer.start();

		buttonInvalido.setEnabled(false);
		buttonValido.setEnabled(false);
	}

	private void disableButtons() {
		buttonInvalido.setEnabled(false);
		buttonValido.setEnabled(false);
		// button3.setEnabled(false);
	}

	private void addSuspect(Object boxName) {

		int count = validator.get(boxName) != null ? validator.get(boxName) : 0;

		validator.put(boxName, ++count);

	}

	private void addInvalid(File file) {

		int count = validatorInvalid.get(file.getName()) != null ? validatorInvalid
				.get(file.getName()) : 0;

		validatorInvalid.put(file.getName(), ++count);

	}

	private void terminate() {

		System.out.println("Processo terminado com sucesso.");

		Iterator<Object> iter = validator.keySet().iterator();

		while (iter.hasNext()) {
			Object caixa = iter.next();
			System.out.println(caixa + " arquivos suspeitos: "
					+ validator.get(caixa));

			int invalidos = validatorInvalid.get(caixa) != null ? validatorInvalid
					.get(caixa) : 0;
			System.out.println(caixa + " arquivos inválidos: " + invalidos);

		}
	}

	public JLabel getFileProcessing() {
		return fileProcessing;
	}

	public static void setFileProcessing() {
		fileProcessing.setText(getLabelFileProcessing());
	}
}
