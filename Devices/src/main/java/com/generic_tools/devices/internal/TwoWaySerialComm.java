package com.generic_tools.devices.internal;

import com.generic_tools.devices.SerialConnection;
import com.generic_tools.logger.Logger;
import gnu.io.*;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.PortUnreachableException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * TwoWaySerialComm have the ability to send and receive packets using USB serial device.
 * 
 * @author taljmars
 *
 */
public class TwoWaySerialComm implements SerialConnection {

	private String PORT_NAME = null;// = "COM9";
	//private final static int BAUD_RATE = 57600;
	private int BAUD_RATE;// = 115200;

	private SerialPort serialPort;

	private InputStream in;
	private OutputStream out;
	private Logger logger;

	private static int called;

	/* Collecting statistics */
	// RX, TX
	private long receivedBytes = 0;
	private long transmittedBytes = 0;

	// Received bps
	private long lastReadTimestamp = 0;
	private long bytesSinceLastRead = 0;
	private long receivedBytesPerSecond = 0;

	// Transmitter bps
	private long lastWriteTimestamp = 0;
	private long bytesSinceLastWrite = 0;
	private long transmittedBytesPerSecond = 0;

	private boolean connected = false;

	public TwoWaySerialComm(Logger logger) {this.logger = logger;}

	@Override
	public void setBaud(Integer boud) {
		BAUD_RATE = boud;
	}

	@Override
	public Integer getBaud() {
		return BAUD_RATE;
	}

	@Override
	public void setPortName(String port_name) {
		PORT_NAME = port_name.substring(0, port_name.indexOf(" "));
	}

	@Override
	public String getPortName() {
		return PORT_NAME;
	}

	/**
	 * This function try to connect the default port defined. 
	 */
	public boolean connect() {
		logger.LogGeneralMessege("Radio communication manager created");

		try {
			if (PORT_NAME == null)
				return false;

			if (!connect(PORT_NAME))
				return false;

			resetCounters();
		}
		catch (NoSuchPortException e) {
			logger.LogErrorMessege("'" + PORT_NAME + "' port was not found");
			return false;
		}
		catch (PortInUseException e) {
			logger.LogErrorMessege("'" + PORT_NAME + "' port is in use");
			return false;
		}
		catch (Exception e) {
			logger.LogErrorMessege("Unexpected Error:");
			logger.LogErrorMessege(e.getMessage());
			return false;
		}

		logger.LogGeneralMessege("Radio communication manager started successfully");

		return true;
	}

	/**
	 * This function try to connect to a specific port 
	 * 
	 * @param portName
	 * @throws Exception
	 */
	private boolean connect( String portName ) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier( portName );
		if( portIdentifier.isCurrentlyOwned()) {
			logger.LogErrorMessege(portName + " is occupied");
			logger.LogErrorMessege("Port " + portName + " is currently in use");
			throw new PortInUseException();
		}
		else {
			logger.LogGeneralMessege(portName + " is not occupied by this application");
		}

		int timeout = 2000;
		CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );

		if( commPort instanceof SerialPort ) {
			System.out.println("Going to connect to port '" + PORT_NAME + "' with baud rate '" + BAUD_RATE + "'");
			logger.LogDesignedMessege("Going to connect to port '" + PORT_NAME + "' with baud rate '" + BAUD_RATE + "'");
			serialPort = ( SerialPort )commPort;
			serialPort.setSerialPortParams( BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();

			// Statistics
			lastReadTimestamp = lastWriteTimestamp = System.currentTimeMillis();
		} 
		else {
			logger.LogErrorMessege("Port " + portName + " is currently in use");
			logger.LogErrorMessege("Only serial ports are handled");
			throw new PortUnreachableException("Only serial ports are handled");
		}

		this.connected = true;

		return true;
	}

	@Override
	public boolean disconnect() {
		try {
			logger.LogErrorMessege("Disconnected");
			this.connected = false;
			if (out != null)
				out.close();
			out = null;

			if (in != null)
				in.close();
			in = null;

			if (serialPort != null) {
				serialPort.disableReceiveFraming();
				serialPort.disableReceiveThreshold();
				serialPort.disableReceiveTimeout();
				serialPort.removeEventListener();
				serialPort.close();
			}
			serialPort = null;
			return true;
		} catch (IOException e) {
			logger.LogErrorMessege("Failed to disconnect");
			logger.LogErrorMessege(e.getMessage());
			return false;
		}
	}

	@Override
	public boolean isConnect() {
		return connected;
	}

	/**
	 * read byte after byte from the USB device
	 * 
	 * @param readData 	- buffer for reading data
	 * @param len		- size of the buffer
	 * @return
	 */
	public int read(byte[] readData, int len) {
		try {
			int i = 0;
			int b = '\n';

//			while ((b = this.in.read()) != '\n' && b != -1) {
			if (this.in == null) {
//				logger.LogErrorMessege("In stream is not initialized");
				return 0;
			}
			while ((b = this.in.read()) != -1) {
				if (i == len) {
//					throw new Exception("Buffer Overflow");
					logger.LogErrorMessege("Buffer Overflow [exceeded " + len + " bytes]");
					break;
				}
				readData[i++] = (byte) b;
			}

			// Statistics
			bytesSinceLastRead += i;
			receivedBytes += i;
			if (System.currentTimeMillis() - lastReadTimestamp > 1000) {
				receivedBytesPerSecond = (long) ((1.0 * bytesSinceLastRead) /
						((System.currentTimeMillis() - lastReadTimestamp) / 1000));
				lastReadTimestamp = System.currentTimeMillis();
				bytesSinceLastRead = 0;
			}

			return i;
		}
		catch (AccessDeniedException e) {
			logger.LogErrorMessege("Failed to access device, check connectivity");
			e.printStackTrace();
			System.exit(-4);
		}
		catch (IOException e) {
			logger.LogErrorMessege("Failed to read from device");
			logger.close();
			e.printStackTrace();
			System.exit(-5);
		}
		catch (Exception e) {
			logger.LogErrorMessege("Unexpected Error:");
			logger.LogErrorMessege(e.getMessage());
			logger.close();
			e.printStackTrace();
			System.exit(-6);
		}

		return -1;
	}

	/**
	 * write text byte after byte to the USB device
	 *
	 * @param text
	 */
	public void write(String text) {
		try {
			if (this.out == null) {
				logger.LogErrorMessege("Out stream wan't initialed");
				return;
			}
			System.out.println("Sending: " + text);
			if (text != null && this.out != null)
				this.out.write( (text + "\n").getBytes() );
		}
		catch (AccessDeniedException e) {
			logger.LogErrorMessege("Failed to access device, check connectivity");
			logger.close();
			System.exit(-1);
		}
		catch (IOException e) {
			logger.LogErrorMessege("Failed to write to device");
			logger.close();
			e.printStackTrace();
			System.exit(-2);
		}
		catch (Exception e) {
			logger.LogErrorMessege("Unexpected Error:");
			logger.LogErrorMessege(e.getMessage());
			logger.close();
			e.printStackTrace();
			System.exit(-3);
		}
	}

	@Override
	public void write(byte[] buffer) {
		try {
			out.write(buffer);

			// Statistics
			bytesSinceLastWrite += buffer.length;
			transmittedBytes += buffer.length;
			if (System.currentTimeMillis() - lastWriteTimestamp > 1000) {
				transmittedBytesPerSecond = (long) ((1.0 * bytesSinceLastWrite) /
							((System.currentTimeMillis() - lastWriteTimestamp) / 1000) );
				lastWriteTimestamp = System.currentTimeMillis();
				bytesSinceLastWrite = 0;
			}
		}
		catch (IOException e) {
			logger.LogErrorMessege("Failed to write messages");
			logger.LogErrorMessege(e.getMessage());
			logger.close();
			e.printStackTrace();
			System.exit(-1); //For develop purpose TODO: remove it one stabilize
		}
	}

	/**
	 * get available USB port with devices connected to the machine
	 * 
	 * @return String array of available ports
	 */
	@SuppressWarnings("unchecked")
	public Object[] listPorts()
	{
		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		ArrayList<String> ans = new ArrayList<String>();
		int i = 0;
		while ( portEnum.hasMoreElements() ) {
//			System.out.println((i++) + "");
			CommPortIdentifier portIdentifier = portEnum.nextElement();
			ans.add(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()));
		}
		return ans.toArray();
	}

	/**
	 * get the port type name
	 * 
	 * @param portType id
	 * @return port type name
	 */
	private String getPortTypeName ( int portType )
	{
		switch ( portType )
		{
//		case CommPortIdentifier.PORT_I2C:
//			return "I2C";
//		case CommPortIdentifier.PORT_PARALLEL:
//			return "Parallel";
//		case CommPortIdentifier.PORT_RAW:
//			return "Raw";
//		case CommPortIdentifier.PORT_RS485:
//			return "RS485";
		case CommPortIdentifier.PORT_SERIAL:
			return "Serial";
		default:
			return "unknown type";
		}
	}

	@Override
	public Integer[] baudList() {
		Integer[] oblist = new Integer[]{57600, 115200};// Arrays.asList(57600, 115200).toArray();
		return oblist;
	}

	@Override
	public Integer getDefaultBaud() {
		return 115200;
	}

	@Override
	public long getReceivedBytesPerSeconds() {
		return receivedBytesPerSecond;
	}

	@Override
	public long getTransmittedBytesPerSeconds() {
		return transmittedBytesPerSecond;
	}

	@Override
	public long getTx() {
		return transmittedBytes;
	}

	@Override
	public long getRx() {
		return receivedBytes;
	}

	@Override
	public void resetCounters() {
		receivedBytes = 0;
		transmittedBytes = 0;

		// Received bps
		lastReadTimestamp = 0;
		bytesSinceLastRead = 0;
		receivedBytesPerSecond = 0;

		// Transmitter bps
		lastWriteTimestamp = 0;
		bytesSinceLastWrite = 0;
		transmittedBytesPerSecond = 0;
	}
}