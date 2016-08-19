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
import java.awt.Font;
import java.awt.LayoutManager;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Logger;

class ProcessingPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea textArea = new JTextArea();
	private JScrollPane scrollPane;
	private static Logger logger = Logger.getLogger(ProcessingPanel.class);
	private TeeOutputStream stdTeeOut;
	private TeeOutputStream errTeeOut;

	public ProcessingPanel(LayoutManager layout) {
		super(layout);
		textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);		//this.setPreferredSize(new Dimension(700, 700));
		scrollPane = new JScrollPane(textArea);
		//scrollPane.setPreferredSize(new Dimension(680, 680));
		textArea.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		EnhancedPatternLayout lay = new EnhancedPatternLayout("%20.20c{1} %-5p %M %m%n");
		TextAreaAppender taAppender = new TextAreaAppender(textArea);
		taAppender.setThreshold(org.apache.log4j.Level.DEBUG);
		taAppender.setLayout(lay);
		taAppender.activateOptions();
		Logger.getRootLogger().addAppender(taAppender);
		this.add(scrollPane);
		}

	public boolean process(File f, String charset) {
		// one at a time

		//System.out.println("Processing " + f.getAbsolutePath());
		try {
			ChartProcessor processor = new ChartProcessor(true, textArea);
			redirectSystemStreams();
			logger.info("Processing " + f.getAbsolutePath());
			processor.processChart(f, true, charset);

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

		JTextAreaOutputStream taOut = new JTextAreaOutputStream(textArea);
		try {
			stdTeeOut = new TeeOutputStream(System.out, taOut);
			errTeeOut = new TeeOutputStream(System.err, taOut);
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(ProcessingPanel.class.getName()).log(Level.SEVERE, null, ex);
		}

		System.setOut(new PrintStream(stdTeeOut, true));
		System.setErr(new PrintStream(errTeeOut, true));
	}

}
