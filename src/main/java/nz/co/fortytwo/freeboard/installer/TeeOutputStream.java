package nz.co.fortytwo.freeboard.installer;

import java.io.*;

/**
 *
 * @author rberliner
 * Code taken from http://www.feldt.com/work/projects/examples/com/feldt/examples/io/TeeOutputStream.java
 */


public class TeeOutputStream extends OutputStream {
	OutputStream ostream1, ostream2;

	/** sole TeeOutputStream constructor */
	TeeOutputStream(OutputStream o1, OutputStream o2) throws IOException {
		ostream1 = o1;
		ostream2 = o2;
	}

	public void close() throws IOException {
		ostream1.close();
		ostream2.close();
	}

	public void flush() throws IOException {
		ostream1.flush();
		ostream2.flush();
	}

	public void write(int b) throws IOException {
		byte[] buf = new byte[1];
		buf[0] = (byte)b;
		write(buf, 0, 1);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		ostream1.write(b, off, len);
		ostream2.write(b, off, len);
	}
}