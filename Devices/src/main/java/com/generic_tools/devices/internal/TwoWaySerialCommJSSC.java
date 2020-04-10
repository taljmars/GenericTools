package com.generic_tools.devices.internal;

import com.generic_tools.devices.SerialConnection;
import com.generic_tools.logger.Logger;
import jssc.*;

/**
 * TwoWaySerialComm have the ability to send and receive packets using USB serial device.
 * 
 * @author taljmars
 *
 */
public class TwoWaySerialCommJSSC implements SerialConnection, SerialPortEventListener {

	private String PORT_NAME = null;// = "COM9";
	//private final static int BAUD_RATE = 57600;
	private int BAUD_RATE;// = 115200;

	private SerialPort serialPort;

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


	public TwoWaySerialCommJSSC(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void setBaud(Integer boud) {
		BAUD_RATE = boud;
	}

	@Override
	public void setPortName(String port_name) {
		if (port_name.contains(" "))
			PORT_NAME = port_name.substring(0, port_name.indexOf(" "));
		else
			PORT_NAME = port_name;
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
//		catch (NoSuchPortException e) {
//			logger.LogErrorMessege("'" + PORT_NAME + "' port was not found");
//			return false;
//		}
//		catch (PortInUseException e) {
//			logger.LogErrorMessege("'" + PORT_NAME + "' port is in use");
//			return false;
//		}
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
		serialPort = new SerialPort(portName);


		serialPort.openPort();//Open port
		System.out.println("Using " + serialPort.getPortName());
		serialPort.setParams(
				BAUD_RATE,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
//		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		serialPort.writeBytes("This is a test string".getBytes());//Write data to port
//            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
//            serialPort.setEventsMask(mask);//Set mask
//		serialPort.addEventListener(this);//Add SerialPortEventListener


		return true;
	}

	@Override
	public boolean disconnect() {
		try {
			logger.LogErrorMessege("Disconnected");
			return serialPort.closePort();
		}
		catch (SerialPortException e) {
			logger.LogErrorMessege("Failed to disconnect");
			logger.LogErrorMessege(e.getMessage());
			return false;
		}
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

			byte[] buffer = serialPort.readBytes();
			if (buffer == null || buffer.length == 0)
				return 0;

			if (buffer.length > len) {
				System.err.println("Buffer Overflow [exceeded " + len + " bytes]");
				logger.LogErrorMessege("Buffer Overflow [exceeded " + len + " bytes]");
				return 0;
			}

			for ( ; i < buffer.length; i++)
				readData[i] = buffer[i];


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
//		catch (AccessDeniedException e) {
//			logger.LogErrorMessege("Failed to access device, check connectivity");
//			e.printStackTrace();
//			System.exit(-4);
//		}
//		catch (IOException e) {
//			logger.LogErrorMessege("Failed to read from device");
//			logger.close();
//			e.printStackTrace();
//			System.exit(-5);
//		}
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
		if (this.serialPort == null) {
			logger.LogErrorMessege("Out stream wan't initialed");
			return;
		}
		System.out.println("Sending: " + text);
		if (text != null && this.serialPort != null)
			this.write( (text + "\n").getBytes() );
	}

	@Override
	public void write(byte[] buffer) {
		try {
			serialPort.writeBytes(buffer);

//			System.out.println("wrote " + bytesWritten + " bytes");
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
		catch (SerialPortException e) {
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
		String[] portNames = SerialPortList.getPortNames();
		for(int i = 0; i < portNames.length; i++){
			System.out.println("Port Name " + portNames[i]);
		}

		return portNames;
	}

	@Override
	public Integer[] baudList() {
		Integer[] oblist = new Integer[]{57600, 115200};// Arrays.asList(57600, 115200).toArray();
		return oblist;
	}

	@Override
	public Integer getDefaultBaud() {
		return 57600;
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

	@Override
	public void serialEvent(SerialPortEvent event) {

		if(event.isRXCHAR()){//If data is available
			try {
				byte buffer[] = serialPort.readBytes();//10);
				System.out.println("Len " + buffer.length);
			}
			catch (SerialPortException e) {
				e.printStackTrace();
			}
		}
		else if(event.isCTS()){//If CTS line has changed state
			if(event.getEventValue() == 1){//If line is ON
				System.out.println("CTS - ON");
			}
			else {
				System.out.println("CTS - OFF");
			}
		}
		else if(event.isDSR()){///If DSR line has changed state
			if(event.getEventValue() == 1){//If line is ON
				System.out.println("DSR - ON");
			}
			else {
				System.out.println("DSR - OFF");
			}
		}
	}
}