package com.earq.getnet.qa;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
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

	private File[] files;
	private File[] boxes;
	private int indexFile = 0;
	private int indexBox = 0;
	private JLabel labelFileName; 
	private JLabel labelBoxName;
	// private JLabel labelEC;
	private JButton button;
	private JButton button2;
	private JButton button3;
	private JFileChooser fileChooser;
	private SwingController controller;
	private JFrame window;
	private XMLViewer xmlViewer;
	public static String DEFAULT_FOLDER = "C:\\TEMP\\GetNetAmostragem\\Semana x";
	public static int DEFAULT_TIMER_NEXT_WAIT = 3000;

	Map<String, Integer> validator = new HashMap<String, Integer>();
	Map<String, Integer> validatorInvalid = new HashMap<String, Integer>();

	public abstract boolean validateFile(File file);

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

			readWeek(fileChooser.getSelectedFile());
		}

	}

	public void readWeek(File filesPath) {
		// File filesPath = new File(filePath);

		if (filesPath.isDirectory()) {

			FileFilter directoryFilter = new FileFilter() {
				public boolean accept(File file) {
					return file.isDirectory()
							&& file.getName().toLowerCase().contains("caixa");
				}
			};

			boxes = filesPath.listFiles(directoryFilter);

			if (boxes.length > 1) {
				System.out.println("Lendo caixa: " + boxes[indexBox].getName());

				if (boxes[indexBox].isDirectory()) {

					FileFilter fileFilter = new FileFilter() {
						public boolean accept(File file) {
							return !file.isDirectory()
									&& file.getName().toLowerCase()
											.contains("pdf");
						}
					};
					files = boxes[indexBox].listFiles(fileFilter);
					readBox();
				}
			} else {
				JOptionPane
						.showMessageDialog(null, "Nenhuma caixa encontrada.");
			}
		}
	}

	public void readBox() {

		boolean validFile = validateFile(files[indexFile]);

		if (!validFile) {
			addSuspect(boxes[indexBox]);
			showPanel();
		} else {
			// showPanel();
			nextFile(window);
		}

	}

	public void setProperties() {

//		System.getProperties().put("application.viewerpreferences.hidemenubar",
//				"true");

	}

	public void showPanel() {

		setProperties();

		File file = files[indexFile];

		JPanel header = new JPanel();

		JLabel labelFolder = new JLabel(
				getLabelFolder(fileChooser.getSelectedFile()));

		header.add(labelFolder, BorderLayout.PAGE_START);

		labelFileName = new JLabel(getLabelFileName(file));
		labelBoxName = new JLabel(getLabelBoxName(file));
		// labelEC = new JLabel(getLabelEC(file));

		controller = new SwingController();

		PropertiesManager properties = new PropertiesManager(
				System.getProperties(),
				ResourceBundle
						.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));

		properties.set(PropertiesManager.PROPERTY_DEFAULT_ZOOM_LEVEL, "3");

//		properties.setBoolean(
//				PropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION,
//				Boolean.FALSE);
//		properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_FIT,
//				Boolean.FALSE);

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

		button = new JButton("<html><h1><font color='red'>Inválido</font></h1>");

		// Add action listener to button1
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed

				doArquivoInvalido(files[indexFile]);

				addInvalid(boxes[indexBox]);

				nextFile(window);

				disableButtons();
			}
		});

		buttons.add(button, BorderLayout.CENTER);

		button2 = new JButton(
				"<html><h1><font color='green'>&nbsp;&nbsp;Válido&nbsp;&nbsp;</font></h1>");

		// Add action listener to button2
		button2.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed

				doArquivoValido(files[indexFile]);
				nextFile(window);
				disableButtons();

			}
		});

		buttons.add(button2, BorderLayout.CENTER);

		button3 = new JButton(
				"<html><h1><font color='blue'>&nbsp;&nbsp;&nbsp;XML&nbsp;&nbsp;&nbsp;</font></h1>");
		// Add action listener to button2
		button3.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed

				try {

					if (xmlViewer == null) {
						xmlViewer = new XMLViewer(window);
					}

					String fileName = files[indexFile].getName();
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
					fileName += ".xml";

					xmlViewer.viewXML(files[indexFile].getParent()
							+ File.separator + fileName);

				} catch (Exception e1) {
					System.out.println("Erro ao abrir o XML: "
							+ e1.getMessage());
				}
			}
		});

		buttons.add(button3, BorderLayout.CENTER);

		window.add(buttons, BorderLayout.PAGE_END);

		JPanel filePanel = new JPanel();

		filePanel.add(labelFileName);
		filePanel.add(labelBoxName);
		// filePanel.add(labelEC);
//		filePanel.add(viewerComponentPanel);
//		filePanel.setEnabled(false);

		window.add(filePanel);
		window.add(viewerComponentPanel);
		
		window.pack();
		window.setExtendedState(window.MAXIMIZED_BOTH);
		window.setVisible(true);

		controller.openDocument(files[indexFile].getAbsolutePath());
		delayButtons();

	}



	protected void nextFile(JFrame window) {
		if (indexFile == files.length - 1) {
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
			files = boxes[++indexBox].listFiles();

		} else {
			indexFile++;
		}

		File next = files[indexFile];
		if (!next.getName().toLowerCase().endsWith("pdf")) {
			nextFile(window);

		} else {

			boolean validFile = validateFile(next);

			if (!validFile) {
				if (window == null) {
					showPanel();
				} else {
					addSuspect(boxes[indexBox]);
					labelFileName.setText(getLabelFileName(next));
					labelBoxName.setText(getLabelBoxName(next));
					// labelEC.setText(getLabelEC(next));
					controller.openDocument(files[indexFile].getAbsolutePath());
					controller.setPageViewMode(
							DocumentViewControllerImpl.ONE_PAGE_VIEW, false);
				}

				delayButtons();

			} else {
				nextFile(window);
			}

		}

	}
	
	
	private String getLabelFileName(File file) {

		String unpadded = file.getName();
		String padded = "";
		String mask = "_____________________________";

		if (file.getName().length() <= mask.length()) {
			padded = mask.substring(unpadded.length());
		}

		return "<html><br/><h2>Arquivo:&nbsp;<font color='blue'>"
				+ file.getName() + "</font><font color='white'>" + padded
				+ "</font>&nbsp;&nbsp;&nbsp;</h2> ";
	}

	private String getLabelBoxName(File file) {
		return "<html><br/><h2>Caixa:&nbsp;<font color='blue'>"
				+ boxes[indexBox].getName()
				+ "</font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</h2> ";
	}

	// private String getLabelEC(File file) {
	// return "<html><br/><h2>EC:&nbsp;<font color='blue'>" + "TODO"
	// + "</font>&nbsp;&nbsp;&nbsp;</h2>";
	// }

	private String getLabelFolder(File file) {
		return "<html><br/><h2>Lendo Diretório:&nbsp;&nbsp;&nbsp;&nbsp;<font color='blue'>"
				+ file.getAbsolutePath() + "</font>&nbsp;&nbsp;&nbsp;</h2>";
	}

	private void delayButtons() {
		
		Timer timer = new Timer(DEFAULT_TIMER_NEXT_WAIT, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				button.setEnabled(true);
				button2.setEnabled(true);
			}

		});
		timer.setRepeats(false);

		timer.start();

		button.setEnabled(false);
		button2.setEnabled(false);
	}

	private void disableButtons() {
		button.setEnabled(false);
		button2.setEnabled(false);
		// button3.setEnabled(false);
	}

	private void addSuspect(File file) {

		int count = validator.get(file.getName()) != null ? validator.get(file
				.getName()) : 0;

		validator.put(file.getName(), ++count);

	}

	private void addInvalid(File file) {

		int count = validatorInvalid.get(file.getName()) != null ? validatorInvalid
				.get(file.getName()) : 0;

		validatorInvalid.put(file.getName(), ++count);

	}

	private void terminate() {

		System.out.println("Processo terminado com sucesso.");

		Iterator<String> iter = validator.keySet().iterator();

		while (iter.hasNext()) {
			String caixa = iter.next();
			System.out.println(caixa + " arquivos suspeitos: "
					+ validator.get(caixa));

			int invalidos = validatorInvalid.get(caixa) != null ? validatorInvalid
					.get(caixa) : 0;
			System.out.println(caixa + " arquivos inválidos: " + invalidos);

		}
	}

}
