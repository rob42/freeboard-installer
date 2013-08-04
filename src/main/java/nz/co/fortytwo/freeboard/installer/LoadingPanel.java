package nz.co.fortytwo.freeboard.installer;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

class LoadingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea textArea = new JTextArea();
	private JScrollPane scrollPane;

	public LoadingPanel() {
		this.setPreferredSize(new Dimension(500, 700));
		scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(480, 680));
		textArea.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.add(scrollPane);
	}

	public boolean process(File f, String commPort, String device, File toolsDir) {
		// one at a time
		
		System.out.println("Processing " + f.getAbsolutePath());
		try {
			UploadProcessor processor = new UploadProcessor(true, textArea);
			redirectSystemStreams();
			System.out.println("Uploading "+f.getAbsolutePath()+" to "+device+" on "+commPort +", tools at"+toolsDir);
			processor.processUpload(f, commPort, device, toolsDir.getAbsolutePath());

		} catch (Exception e) {
			System.out.print(e.getMessage() + "\n");
			e.printStackTrace();
			return false;
		} 
		return true;
	}

	public void clear() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.setText("");
			}
		});

	}

	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}

}
