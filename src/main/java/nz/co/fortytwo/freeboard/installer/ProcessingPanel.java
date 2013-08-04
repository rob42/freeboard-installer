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

class ProcessingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea textArea = new JTextArea();
	private JScrollPane scrollPane;

	public ProcessingPanel() {
		this.setPreferredSize(new Dimension(500, 700));
		scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(480, 680));
		textArea.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.add(scrollPane);
	}

	public boolean process(File f) {
		// one at a time
		
		System.out.println("Processing " + f.getAbsolutePath());
		try {
			ChartProcessor processor = new ChartProcessor(true, textArea);
			redirectSystemStreams();
			processor.processChart(f, true);

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
