package com.earq.getnet.qa;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.io.FileInputStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class XMLViewer {

	private SAXTreeBuilder saxTree = null;
	private JFrame frame = new JFrame("eBravo XMLTreeView");
	private static String file = "";

	public static void main(String args[]) throws HeadlessException, Exception {

		String file = "C:/TEMP/GetNetAmostragem/OS_000000001609385_796.XML";

		new XMLViewer(new JFrame()).viewXML(file);
	}

	public void viewXML(String fileParam) throws Exception {
		file = fileParam;

		frame.setSize(550, 750);

//		frame.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent ev) {
//				System.exit(0);
//			}
//		});

		new XMLViewer(frame);
	}

	public XMLViewer(JFrame frame) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		frame.getContentPane().setLayout(new BorderLayout());
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(file);
		// DefaultMutableTreeNode top = new
		// DefaultMutableTreeNode("XML Document");

		saxTree = new SAXTreeBuilder(top);

		try {
			SAXParser saxParser = new SAXParser();
			saxParser.setContentHandler(saxTree);
			saxParser.parse(new InputSource(new FileInputStream(file)));
		} catch (Exception ex) {
			top.add(new DefaultMutableTreeNode(ex.getMessage()));
		}
		JTree tree = new JTree(saxTree.getTree());

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		JScrollPane scrollPane = new JScrollPane(tree);

		frame.getContentPane().add("Center", scrollPane);
		frame.setVisible(true);

	}

}

class SAXTreeBuilder extends DefaultHandler {

	private DefaultMutableTreeNode currentNode = null;
	private DefaultMutableTreeNode previousNode = null;
	private DefaultMutableTreeNode rootNode = null;

	public SAXTreeBuilder(DefaultMutableTreeNode root) {
		rootNode = root;
	}

	public void startDocument() {
		currentNode = rootNode;
	}

	public void endDocument() {
	}

	public void characters(char[] data, int start, int end) {
		String str = new String(data, start, end);
		if (!str.equals("") && Character.isLetter(str.charAt(0)))
			currentNode.add(new DefaultMutableTreeNode(str));
	}

	public void startElement(String uri, String qName, String lName,
			Attributes atts) {
		previousNode = currentNode;
		currentNode = new DefaultMutableTreeNode(lName);
		// Add attributes as child nodes //
		attachAttributeList(currentNode, atts);
		previousNode.add(currentNode);
	}

	public void endElement(String uri, String qName, String lName) {
		if (currentNode.getUserObject().equals(lName))
			currentNode = (DefaultMutableTreeNode) currentNode.getParent();
	}

	public DefaultMutableTreeNode getTree() {
		return rootNode;
	}

	private void attachAttributeList(DefaultMutableTreeNode node,
			Attributes atts) {
		for (int i = 0; i < atts.getLength(); i++) {
			
			String name = atts.getLocalName(i);
			String value = atts.getValue(name);
			
						
			
			node.add(new DefaultMutableTreeNode(name + " = " + value));
		}
	}

}
