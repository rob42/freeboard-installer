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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import purejavacomm.CommPortIdentifier;

class CalibratePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea textArea = new JTextArea();
	private JScrollPane scrollPane;
	private StringBuffer rawBuffer= new StringBuffer();
	private CalibrateProcessor processor;
	

	public CalibratePanel() {
		this.setPreferredSize(new Dimension(500, 700));
		scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(480, 680));
		textArea.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.add(scrollPane);
	}

	public void stopProcess() {
		if(processor!=null){
			try {
				System.out.print("Attempting to stop..\n");
				processor.stopRawData();
				//processor=null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(rawBuffer.toString().length()<10){
			System.out.println("Did not collect enough data, try again..\n"+rawBuffer.toString());
			return;
		}

		int[] offsets = new int[6];
		float[] scale = new float[6];
		double [][] fullCompensation = processor.calculate(rawBuffer.toString());
		offsets[0]=(int) fullCompensation[0][0];
		offsets[1]=(int) fullCompensation[1][0];
		offsets[2]=(int) fullCompensation[2][0];
		offsets[3]=(int) fullCompensation[3][0];
		offsets[4]=(int) fullCompensation[4][0];
		offsets[5]=(int) fullCompensation[5][0];
		scale[0]=(float) fullCompensation[0][1];
		scale[1]=(float) fullCompensation[1][1];
		scale[2]=(float) fullCompensation[2][1];
		scale[3]=(float) fullCompensation[3][1];
		scale[4]=(float) fullCompensation[4][1];
		scale[5]=(float) fullCompensation[5][1];
		//format for IMU
		// (int16_t) acc_off_x, acc_off_y, acc_off_z, magn_off_x, magn_off_y, magn_off_z;
		// (float) acc_scale_x, acc_scale_y, acc_scale_z, magn_scale_x, magn_scale_y, magn_scale_z;
		processor.saveToDevice(offsets, scale);
		
	}

	public boolean process(String commPortStr) {
		// one at a time
		//System.out.println("Processing " + f.getAbsolutePath());
		try {
			processor = new CalibrateProcessor(true, textArea);
			redirectSystemStreams();
			rawBuffer=new StringBuffer();
			processor.connect(CommPortIdentifier.getPortIdentifier(commPortStr),rawBuffer);

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
