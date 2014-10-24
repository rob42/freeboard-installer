Freeboard Installer

Installer to aid in freeboard setup tasks. It loads the arduinos, calibrates the IMU, and converts charts.

Its a first cut, and probably going to have some problems. In particular its only tested in a basic way on my Linux dev systems, 
so it may not work as well, or at all on Windows or Mac. If you can test for me that would be appreciated :-)

  For charts, You will need gdal installed on you PC.
  For linux:
    'sudo apt-get install gdal-bin python-gdal imagemagick'

  For windows 64 bit, courtesy of Matthias:

	Download and install python
	Tested with python 3.4.2 from https://www.python.org/downloads/release/python-342/
	Download and install OSGeo4W from http://download.osgeo.org/osgeo4w/osgeo4w-setup.exe
	 
	From here, installation for linux and Win8 is similar

	Start the freeboard-installer:
	* You may be able to double-click the jar file
	OR
	* from the command line: "java -jar freeboard-installer.jar"   

  Go to the chart tab, select you .kap chart file, and process.
	The right-hand window will show the progress and a directory will be created with the mapping tiles etc. There is a zip file of this directory also created which will be moved to the RaspberryPi.
	
	You can go to the newly created directory and open the 'openlayers.html' file in a web browser. You should be able to see you newly created chart, but you may have to zoom in to the correct area if its a small chart.
	
  Copy the yourMap zip file to your raspberry pi, and unzip in the same mapcache directory, so the result is:

    /home/pi/freeboard/mapcache/NZ46/...

  Restart freeboard to use the new chart.

  Your chart should be available in the chart list (top-right button of chartplotter)
  Select the 'Charts' tab and move you charts to the right-hand box.
  
  IMPORTANT: Select "Home" if your Pi is connected to your home network, and 'Boat' if your pi is running on a stand-alone network, eg in your boat.

	Save, and reload/refresh you browser page
  If you see the 'Processing' message forever..., then you need to choose 'Home'. ('Boat' is much faster but has DNS issues on home networks.)

  

Basic process is:

1. Download and install the current Arduino IDE from http://http://arduino.cc/en/Main/Software. 
Install in a directory that does NOT have spaces!! (Same for all the freeboard software)

2. Download the freeboard-installer.jar (above) into a suitable directory

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

