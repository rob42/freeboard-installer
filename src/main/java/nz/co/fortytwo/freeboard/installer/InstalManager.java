/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 *
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 *
 * FreeBoard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FreeBoard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FreeBoard. If not, see <http://www.gnu.org/licenses/>.
 */
package nz.co.fortytwo.freeboard.installer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.ButtonGroup;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import purejavacomm.CommPortIdentifier;

/**
 * @see http://stackoverflow.com/questions/4053090
 */
public class InstalManager extends JFrame {

	/**
	 *
	 */
	private static Logger logger = Logger.getLogger(InstalManager.class);
	private static final long serialVersionUID = 1L;
	private ProcessingPanel processingPanel = new ProcessingPanel(new BorderLayout());
	private LoadingPanel uploadingPanel = new LoadingPanel();
	private CalibratePanel calibrationPanel = new CalibratePanel();
	private ChartFileChooser chartFileChooser = new ChartFileChooser();
	private HexFileChooser hexFileChooser = new HexFileChooser();
	private JFileChooser arduinoIdeChooser = new JFileChooser();
	private JTextField arduinoDirTextField = new JTextField(40);
	private JRadioButton infoButton = new JRadioButton("Info");
	private JRadioButton debugButton = new JRadioButton("Debug");
	private ButtonGroup loggingGroup = new ButtonGroup();
	private JRadioButton utf8Button = new JRadioButton("UTF-8");
	private JRadioButton iso8859Button = new JRadioButton("ISO-8859-1");
	private ButtonGroup charsetGroup = new ButtonGroup();
	private String charset = "UTF-8";
	File toolsDir;
	private long startTime;
	JComboBox<String> deviceComboBox;
	HashMap<String, String> deviceMap = new HashMap<String, String>();
	JComboBox<String> portComboBox;
	JComboBox<String> portComboBox1;

	public InstalManager(String name) {
		super(name);

		String[] devices = new String[]{"ArduIMU v3", "Arduino Mega 1280", "Arduino Mega 2560"};
		deviceMap.put(devices[0], "atmega328p");
		deviceMap.put(devices[1], "atmega1280");
		deviceMap.put(devices[2], "atmega2560");
		deviceComboBox = new JComboBox<String>(devices);

		//toolsDir = new File("./src/main/resources/tools");
		//if (!toolsDir.exists()) {
		//	System.out.println("Cannot locate avrdude");
		//}
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> commPorts = CommPortIdentifier.getPortIdentifiers();
		Vector<String> commModel = new Vector<String>();
		while (commPorts.hasMoreElements()) {
			CommPortIdentifier commPort = commPorts.nextElement();
			if (commPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				//?if(SystemUtils.IS_LINUX())
				if (commPort.getName().startsWith("tty")) {
					// linux
					commModel.add("/dev/" + commPort.getName());
				} else {
					// windoze
					commModel.add(commPort.getName());
				}
			}
		}
		portComboBox = new JComboBox<String>(commModel.toArray(new String[0]));
		portComboBox1 = new JComboBox<String>(commModel.toArray(new String[0]));

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWidgets();
		this.pack();
		this.setVisible(true);
		JOptionPane.showMessageDialog(this, "This utility is quite experimental, specifically I develop on Linux, and I dont have MS Windows \nto test on, so dont be surprised by errors!\n"
				  + "If you have problems, copy the output from the right side panel, along with a clear description \nof what you were doing, and email me at robert@42.co.nz"
				  + " so I can recreate and fix the error."
				  + "\n\nCopyright 2013 R T Huitema. Licenced under GNU GPL v3");
	}

	private void addWidgets() {

		JTabbedPane tabPane = new JTabbedPane();
		this.add(tabPane, BorderLayout.CENTER);
		// upload to arduinos
		JPanel uploadPanel = new JPanel();
		uploadPanel.setLayout(new BorderLayout());
		uploadPanel.add(uploadingPanel, BorderLayout.CENTER);
		final JPanel westUploadPanel = new JPanel(new MigLayout());

		String info = "\nUse this panel to upload compiled code to the arduino devices.\n\n"
				  + "NOTE: directories with spaces will probably not work!\n\n"
				  + "First select the base directory of your Arduino IDE installation, eg C:/devtools/arduino-1.5.2\n\n"
				  + "Then select target files to upload, these are ended in '.hex'\n"
				  + "\nand can be downloaded from github (https://github.com/rob42),\n"
				  + " see the 'Release*' sub-directories\n\n"
				  + "Output of the process will display in the right-side window\n\n";
		JTextArea jTextInfo = new JTextArea(info);
		jTextInfo.setEditable(false);
		westUploadPanel.add(jTextInfo, "span,wrap");

		westUploadPanel.add(new JLabel("Select Arduino IDE directory:"), "wrap");
		arduinoDirTextField.setEditable(false);
		westUploadPanel.add(arduinoDirTextField, "span 2");
		arduinoIdeChooser.setApproveButtonText("Select");
		arduinoIdeChooser.setAcceptAllFileFilterUsed(false);
		arduinoIdeChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		arduinoIdeChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())) {

					toolsDir = new File(arduinoIdeChooser.getSelectedFile(), File.separator + "hardware" + File.separator + "tools" + File.separator);
					if (!toolsDir.exists()) {
						toolsDir = null;
						JOptionPane.showMessageDialog(westUploadPanel, "Not a valid Arduino IDE directory");
						return;
					}
					arduinoDirTextField.setText(arduinoIdeChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		JButton arduinoDirButton = new JButton("Select");
		arduinoDirButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				arduinoIdeChooser.showDialog(westUploadPanel, "Select");

			}
		});
		westUploadPanel.add(arduinoDirButton, "wrap");

		westUploadPanel.add(new JLabel("Select comm port:"));
		westUploadPanel.add(portComboBox, "wrap");

		westUploadPanel.add(new JLabel("Select device:"), "gap unrelated");
		westUploadPanel.add(deviceComboBox, "wrap");

		hexFileChooser.setApproveButtonText("Upload");
		hexFileChooser.setAcceptAllFileFilterUsed(false);
		hexFileChooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "*.hex - Hex file";
			}

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				if (f.getName().toUpperCase().endsWith(".HEX")) {
					return true;
				}
				return false;
			}
		});
		westUploadPanel.add(hexFileChooser, "span, wrap");

		uploadPanel.add(westUploadPanel, BorderLayout.WEST);
		tabPane.addTab("Upload", uploadPanel);

		// charts
		JPanel chartPanel = new JPanel();
		chartPanel.setLayout(new BorderLayout());
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Charts", "tiff", "kap", "KAP", "TIFF", "tif", "TIF");
		chartFileChooser.setFileFilter(filter);
		chartFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chartFileChooser.setMultiSelectionEnabled(true);
		final JPanel chartWestPanel = new JPanel(new MigLayout());
		String info2 = "\nUse this panel to convert charts into the correct format for FreeBoard.\n"
				  + "\nYou need to select the charts or directories containing charts, then click 'Process'.\n "
				  + "\nThe results will be in a directory with the same name as the chart, and the chart "
				  + "\ndirectory will also be compressed into a zip file ready to transfer to your FreeBoard "
				  + "\nserver\n"
				  + "\nOutput of the process will display in the right-side window\n\n";
		JTextArea jTextInfo2 = new JTextArea(info2);
		jTextInfo2.setEditable(false);
		chartWestPanel.add(jTextInfo2, "wrap");

		chartFileChooser.setApproveButtonText("Process");
		chartWestPanel.add(chartFileChooser, "span,wrap");

		final JPanel loggingPanel = new JPanel(new MigLayout());
		loggingGroup.add(infoButton);
		loggingGroup.add(debugButton);
		debugButton.setSelected(logger.isDebugEnabled());
		infoButton.setSelected(!logger.isDebugEnabled());
		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (infoButton.isSelected()) {
					LogManager.getRootLogger().setLevel(Level.INFO);
				}
			}
		});
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (debugButton.isSelected()) {
					LogManager.getRootLogger().setLevel(Level.DEBUG);
				}
			}
		});

		loggingPanel.add(new JLabel("Logging Level"));
		loggingPanel.add(infoButton);
		loggingPanel.add(debugButton);
		chartWestPanel.add(loggingPanel,"span,wrap");
		
		final JPanel transparentPanel = new JPanel(new MigLayout());
		charsetGroup.add(utf8Button);
		charsetGroup.add(iso8859Button);
		iso8859Button.setSelected(logger.isDebugEnabled());
		utf8Button.setSelected(!logger.isDebugEnabled());
		utf8Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (utf8Button.isSelected()) {
					charset="UTF-8";
				}
			}
		});
		iso8859Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (iso8859Button.isSelected()) {
					charset="ISO-8859-1";
				}
			}
		});

		transparentPanel.add(new JLabel("KAP Character set:"));
		transparentPanel.add(utf8Button);
		transparentPanel.add(iso8859Button);
		chartWestPanel.add(transparentPanel);

		chartPanel.add(chartWestPanel, BorderLayout.WEST);
		chartPanel.add(processingPanel, BorderLayout.CENTER);
		tabPane.addTab("Charts", chartPanel);

		// IMU calibration
		JPanel calPanel = new JPanel();
		calPanel.setLayout(new BorderLayout());
		JPanel westCalPanel = new JPanel(new MigLayout());
		String info3 = "\nUse this panel to calibrate your ArduIMU.\n"
				  + "\nYou should do this as near to the final location as possible,\n"
				  + "and like all compasses, as far from wires and magnetic materials \n"
				  + "as possible.\n"
				  + "\nSelect your comm port, then click 'Start'.\n "
				  + "\nSmoothly and steadily rotate the ArduIMU around all 3 axes (x,y,z)\n"
				  + "several times. Then press stop and the calibration will be performed and\n"
				  + "uploaded to the ArduIMU\n\n"
				  + "Output of the process will display in the right-side window\n\n";
		JTextArea jTextInfo3 = new JTextArea(info3);
		jTextInfo3.setEditable(false);
		westCalPanel.add(jTextInfo3, "span, wrap");
		westCalPanel.add(new JLabel("Select comm port:"));

		westCalPanel.add(portComboBox1, "wrap");
		JButton startCal = new JButton("Start");
		startCal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calibrationPanel.process((String) portComboBox.getSelectedItem());
			}
		});
		westCalPanel.add(startCal);
		JButton stopCal = new JButton("Stop");
		stopCal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calibrationPanel.stopProcess();
			}
		});
		westCalPanel.add(stopCal);

		calPanel.add(westCalPanel, BorderLayout.WEST);
		calPanel.add(calibrationPanel, BorderLayout.CENTER);

		tabPane.addTab("Calibration", calPanel);

	}

	class HexFileChooser extends JFileChooser {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void approveSelection() {
			if (toolsDir == null) {
				//need to select arduino dir first
				JOptionPane.showMessageDialog(this, "You must select the Arduino IDE directory first");
				return;
			}
			final File f = hexFileChooser.getSelectedFile();

			new Thread() {
				@Override
				public void run() {
					String device = deviceMap.get(deviceComboBox.getSelectedItem());
					hexFileChooser.setEnabled(false);
					uploadingPanel.process(f, (String) portComboBox.getSelectedItem(), device, toolsDir);
					hexFileChooser.setEnabled(true);
				}
			}.start();
		}

		@Override
		public void cancelSelection() {
			uploadingPanel.clear();

		}
	}

	class ChartFileChooser extends JFileChooser {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void approveSelection() {
			final File[] filesDirs = chartFileChooser.getSelectedFiles();
			if (filesDirs == null || filesDirs.length == 0) {
				JOptionPane.showMessageDialog(this, "No files selected!");
				return;
			}
			startTime = System.currentTimeMillis();
			new Thread() {
				String[] SUFFIX = {"kap", "KAP", "jpg", "jpeg", "JPG", "JPEG"};

				public void run() {
					IOFileFilter filter = new SuffixFileFilter(SUFFIX);
					chartFileChooser.setEnabled(false);
					Collection<File> files = new java.util.LinkedList<File>();

					// If the selection includes a chart file or files, they will be added to the List files
					// If the selection contains directories with charts, the directory tree will be searched
					// recursively to find all of the chart files and they will be add to the List files
					for (File d : filesDirs) {
						if (d.isFile()) {
							files.add(d);
						} else {
							files.addAll((List<File>) (org.apache.commons.io.FileUtils.listFiles(d, filter, TrueFileFilter.INSTANCE)));
						}
					}

					// Process the files
					for (File f : files) {
						logger.info("Processing " + f.getAbsolutePath());
						processingPanel.process(f,charset);
						long elapTime = System.currentTimeMillis() - startTime;
						logger.info("Elapsed time "+elapTime/1000.+"\n");
					}
					chartFileChooser.setEnabled(true);
				}
			}.start();
		}

		@Override
		public void cancelSelection() {

			processingPanel.clear();

		}
	}

	public static void main(String[] args) {
		System.out.println("Current dir:" + new File(".").getAbsolutePath());
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new InstalManager("FreeBoard - Install Manager").setVisible(true);
			}
		});
	}
}
