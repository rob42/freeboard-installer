Freeboard Installer

This is an installer to simplify getting a freeboard installation running. It loads the arduinos, calibrates the IMU, and converts charts.

Its a first cut, and probably going to have some problems. In particular its only tested in a basic way on my Linux dev systems, 
so it may not as well, or at all on Windows or Mac. If you can test for me that would be appreciated :-)

Basic process is:

1. Download and install the current Arduino IDE from http://http://arduino.cc/en/Main/Software. 
Install in a directory that does NOT have spaces!! (Same for all the freeboard software)

2. Download the freeboard-installer.jar () into a suitable directory

3. Download the HEX files for your Arduino Mega (https://github.com/rob42/freeboardPLC/blob/master/Release1280/FreeBoardPLC.hex for Mega 1280, 
 https://github.com/rob42/freeboardPLC/blob/master/Release2560/FreeBoardPLC.hex for 2560)
 
4. Download the ArduIMU hex file (https://github.com/rob42/FreeIMU-20121106_1323/blob/master/FreeBoardIMU/target/FreeBoardIMU.cpp.hex)

5. Plug in your Mega or ArduIMU

6. Start the freeboard-installer:
	* You may be able to double-click the jar file
	OR
	* from the command line: "java -jar freeboard-installer.jar"   

7. Follow the notes on each tab

Feedback welcome:-)
Rob	