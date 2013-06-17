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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTextArea;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.doube.geometry.FitEllipsoid;
import org.doube.jama.Matrix;

import purejavacomm.CommPortIdentifier;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;

/**
 * Collects raw data and creates calibration for ArduIMU
 * 
 * @author robert
 * 
 */
public class CalibrateProcessor {

	static double MAG = 1.0;
	Logger logger = Logger.getLogger(CalibrateProcessor.class);
	private boolean manager = false;
	private JTextArea textArea;
	private SerialPort serialPort;
	private SerialReader serialReader;

	public CalibrateProcessor() throws Exception {

	}

	public CalibrateProcessor(boolean manager, JTextArea textArea) throws Exception {
		this.manager = manager;
		this.textArea = textArea;
	}

	/**
	 * Upload the Hex file to the named port
	 * 
	 * @param rawBuffer
	 * 
	 * @param hexFile
	 * @param device
	 * @throws Exception
	 */
	public void connect(CommPortIdentifier commPort, StringBuffer rawBuffer) throws Exception {

		if (manager) {
			System.out.print("Obtaining calibration data..\n");
			if(serialPort!=null){
				serialPort.close();
			}
			serialPort = (SerialPort) commPort.open("FreeboardSerialReader", 100);
			// TODO: change baud rate to config based setup
			serialPort.setSerialPortParams(38400, 8, 1, 0);
			serialReader = new SerialReader(rawBuffer);
			serialPort.enableReceiveTimeout(1000);
			serialPort.notifyOnDataAvailable(true);
			serialPort.addEventListener(serialReader);
			Thread.sleep(1500);
			serialReader.startRaw();
		}

		logger.debug("Obtaining calibration data..\n");

	}

	public void stopRawData() throws Exception {

		if (manager) {
			System.out.print("Stopping calibration data..\n");
			serialReader.startImu();
			if (serialPort != null) {
				//serialPort.removeEventListener();
				// serialPort.close();
			}

		}

	}

	/**
	 * Save the compensation to the IMU
	 * 
	 * @param compensation
	 */
	public void saveToDevice(int[] offsets, float[] scale) {
		// const uint8_t eepromsize = sizeof(float) * 6 + sizeof(int) * 6;
		// IMU format is:
		// (int16_t) acc_off_x, acc_off_y, acc_off_z, magn_off_x, magn_off_y, magn_off_z;
		// (float) acc_scale_x, acc_scale_y, acc_scale_z, magn_scale_x, magn_scale_y, magn_scale_z;
		System.out.println("Offsets:"+Arrays.toString(offsets));
		System.out.println("Scale:"+Arrays.toString(scale));
		serialReader.save(offsets, scale);

	}

	/** */
	public class SerialReader implements SerialPortEventListener {

		List<String> lines = new ArrayList<String>();
		StringBuffer line = new StringBuffer(60);
		StringBuffer rawBuffer;

		private boolean complete;
		private InputStream in;
		private BufferedOutputStream out;
		private boolean raw=false;

		public SerialReader(StringBuffer rawBuffer) throws Exception {
			this.rawBuffer = rawBuffer;
			// this.in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			this.in = new BufferedInputStream(serialPort.getInputStream());
			this.out = new BufferedOutputStream(serialPort.getOutputStream());

			logger.info("Setup serialReader on :" + serialPort.getName());
		}

		public void save(int[] offsets, float[] scale) {
			// const uint8_t eepromsize = sizeof(float) * 6 + sizeof(int) * 6;
			// IMU format is:
			// (int16_t) acc_off_x, acc_off_y, acc_off_z, magn_off_x, magn_off_y, magn_off_z;
			// (float) acc_scale_x, acc_scale_y, acc_scale_z, magn_scale_x, magn_scale_y, magn_scale_z;
			try {
				//go into save mode
				out.write("#CAL\n".getBytes("US-ASCII"));
				//out.flush();
				for(int i : offsets){
					//write bytes (arduino int = java short) arduino float = java float
					out.write(intToShortBytes(i));
				}
				for(float f: scale ){
					out.write(floatToBytes(f));
				}
				//out.write(13);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private byte[] floatToBytes(float f) {
			int fInt = Float.floatToIntBits(f);
			byte[] arr=new byte[]{(byte)(fInt&0xFF),(byte)(fInt>>>8),(byte)(fInt>>>16),(byte)(fInt>>>24)};
			return arr;
		}

		public byte[] intToShortBytes(int d) {
			short s = Integer.valueOf(d).shortValue();
	        //return ByteBuffer.allocate(2).putShort(s).array();
			byte[] arr=new byte[]{(byte)(s&0xFF),(byte)(s>>>8)};
			return arr;
	    }

		public void startRaw() {
			try {
				rawBuffer.delete(0, rawBuffer.length());
				out.write("#RAW\n".getBytes("US-ASCII"));
				out.flush();
				raw = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void startImu() {
			try {
				out.write("#IMU\n".getBytes("US-ASCII"));
				out.flush();
				raw=false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// @Override
		public void serialEvent(SerialPortEvent event) {
			logger.debug("SerialEvent:" + event.getEventType());
			try {
				if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

					int r = 0;
					byte[] buff = new byte[256];
					int x = 0;
					while (r > -1) {
						try {
							r = in.read();
							buff[x] = (byte) r;
							x++;

							// 10=LF, 13=CR, lines should end in CR/LF
							if (r == 10 || x == 256) {
								if (r == 10)
									complete = true;
								line.append(new String(buff));
								buff = new byte[256];
								x = 0;
							}

						} catch (IOException e) {
							logger.error(serialPort.getName() + ":" + e.getMessage());
							logger.debug(e.getMessage(), e);
							return;
						}
						// we have a line ending in CR/LF
						if (complete) {
							String lineStr = line.toString().trim();
							// its not empty!
							if (lineStr.length() > 0) {
								//we only want x,x,x,x,x,x lines
								if (raw && lineStr.matches("[0-9.-]*,[0-9.-]*,[0-9.-]*,[0-9.-]*,[0-9.-]*,[0-9.-]*")) {
									//textArea.append(lineStr + "\n");
									rawBuffer.append(lineStr + "\n");
									//logger.debug(lineStr);
								}else{
									if(lineStr.indexOf("$")<0 && lineStr.indexOf(":")<0){
										textArea.append(lineStr + "\n");
									}
								}
							}
							complete = false;
							line = new StringBuffer(60);
						}
					}
				}
			} catch (Exception e) {
				logger.error(serialPort.getName(), e);
			}

		}

	}

	static double[][] calculate(double[][] points) throws IOException {
		// Earth Mag field norm= 0.569
		System.out.println("Fit array to ellipsoid, yuriPetrov..");

		Object[] ellipsoid = FitEllipsoid.yuryPetrov(points);
		double[] centroid = (double[]) ellipsoid[0];
		double[] radii = (double[]) ellipsoid[1];

		Matrix comp = calculateCompensation(ellipsoid);

		System.out.println("Raw Centroid X = " + centroid[0]);
		System.out.println("Raw Centroid Y = " + centroid[1]);
		System.out.println("Raw Centroid Z = " + centroid[2]);
		System.out.println("Raw Radius 1 = " + radii[0]);
		System.out.println("Raw Radius 2 = " + radii[1]);
		System.out.println("Raw Radius 3 = " + radii[2]);

		System.out.println("");
		System.out.println("Offset X = " + centroid[0]);
		System.out.println("Offset Y = " + centroid[1]);
		System.out.println("Offset Z = " + centroid[2]);
		System.out.println("Scale X = " + radii[0]);
		System.out.println("Scale Y = " + radii[1]);
		System.out.println("Scale Z = " + radii[2]);

		double[][] compensation = new double[3][2];
		compensation[0][0] = centroid[0];
		compensation[1][0] = centroid[1];
		compensation[2][0] = centroid[2];
		compensation[0][1] = radii[0];
		compensation[1][1] = radii[1];
		compensation[2][1] = radii[2];
		
		points = applyCompenstation(points, centroid, comp);
		ellipsoid = FitEllipsoid.yuryPetrov(points);
		centroid = (double[]) ellipsoid[0];
		radii = (double[]) ellipsoid[1];

		System.out.println("");
		System.out.println("Adjusted Centroid X = " + centroid[0]);
		System.out.println("Adjusted Centroid Y = " + centroid[1]);
		System.out.println("Adjusted Centroid Z = " + centroid[2]);
		System.out.println("Adjusted Radius 1 = " + radii[0]);
		System.out.println("Adjusted Radius 2 = " + radii[1]);
		System.out.println("Adjusted Radius 3 = " + radii[2]);

		return compensation;
		
	}

	protected static double[][] applyCompenstation(double[][] points, double[] centroid, Matrix comp) {
		for (double[] line : points) {
			line[0] = line[0] - centroid[0];
			line[1] = line[1] - centroid[1];
			line[2] = line[2] - centroid[2];
		}
		Matrix pointsM = new Matrix(points);
		pointsM = pointsM.times(comp);
		return pointsM.getArray();
	}

	/**
	 * Calculate the compensation matrix for the ellipse to sphere
	 * 
	 * @param ellipsoid
	 * @return
	 */
	private static Matrix calculateCompensation(Object[] ellipsoid) {
		// double[] centroid = (double[]) ellipsoid[0];
		double[] radii = (double[]) ellipsoid[1];
		double[][] eVec = (double[][]) ellipsoid[2];
		// double[] eqVars = (double[]) ellipsoid[3];

		// % do ellipsoid fitting
		// [e_center, e_radii, e_eigenvecs, e_algebraic] = ellipsoid_fit([x, y, z]);
		// compensate distorted magnetometer data
		// e_eigenvecs is an orthogonal matrix, so ' can be used instead of inv()
		// S = [x - e_center(1), y - e_center(2), z - e_center(3)]'; % translate and make array

		// scale = inv([e_radii(1) 0 0; 0 e_radii(2) 0; 0 0 e_radii(3)]) * min(e_radii); % scaling matrix
		double[][] scale = new double[3][3];
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				scale[x][y] = 0;
			}
		}
		scale[0][0] = radii[0];
		scale[1][1] = radii[1];
		scale[2][2] = radii[2];
		// invert this
		Matrix scaleM = new Matrix(scale);
		scaleM = scaleM.inverse().times(MAG);

		// map = e_eigenvecs'; % transformation matrix to map ellipsoid axes to coordinate system axes
		Matrix eVecM = new Matrix(eVec);
		Matrix map = eVecM.inverse();

		// invmap = e_eigenvecs; % inverse of above
		Matrix invmap = eVecM;
		// comp = invmap * scale * map;
		Matrix comp = invmap.times(scaleM).times(map);

		scaleM.print(14, 12);
		comp.print(14, 12);
		return comp;
	}

	/**
	 * Takes a string of lines of ax,ay,az,mx,my,mz and returns compensation matrix
	 * Returns a double [6][2] offsets (*o) and scale (*s) = ([axo,ayo,azo,mxo,myo,mzo],[axs,ays,azs,mxs,mys,mzs])
	 * [x][0] = centroid offsets (subtract from raw)
	 * [x][1] = scale factor (divide raw by this)
	 * 
	 * @param data
	 * @return
	 */
	public double[][] calculate(String data) {
		//

		String[] listLines = data.split("\n");
		System.out.println("Create array..");
		double[][] accPoints = new double[listLines.length][3];
		double[][] magPoints = new double[listLines.length][3];
		// populate array
		int c = 0;
		for (String line : listLines) {
			//System.out.println("Line content = "+line);
			if(StringUtils.isBlank(line))continue;
			String[] xyz = line.split(",");
			try{
				// System.out.println("  Line:"+Arrays.toString(xyz));
				accPoints[c][0] = Double.valueOf(xyz[0]);
				accPoints[c][1] = Double.valueOf(xyz[1]);
				accPoints[c][2] = Double.valueOf(xyz[2]);
				
				magPoints[c][0] = Double.valueOf(xyz[3]);
				magPoints[c][1] = Double.valueOf(xyz[4]);
				magPoints[c][2] = Double.valueOf(xyz[5]);
			}catch(Exception e){
				System.out.println("Bad line at "+c+", content = "+line);
				//e.printStackTrace();
			}
			c++;
		}
		try {
			double[][] accComp = calculate(accPoints);
			double[][] magComp = calculate(magPoints);
			double[][] fullComp = new double[6][2];
			fullComp[0][0]=accComp[0][0];
			fullComp[1][0]=accComp[1][0];
			fullComp[2][0]=accComp[2][0];
			fullComp[0][1]=accComp[0][1];
			fullComp[1][1]=accComp[1][1];
			fullComp[2][1]=accComp[2][1];
			fullComp[3][0]=magComp[0][0];
			fullComp[4][0]=magComp[1][0];
			fullComp[5][0]=magComp[2][0];
			fullComp[3][1]=magComp[0][1];
			fullComp[4][1]=magComp[1][1];
			fullComp[5][1]=magComp[2][1];
			
			return fullComp;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
