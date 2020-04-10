//package com.generic_tools.devices.internal;
//
//import com.generic_tools.devices.SerialConnection;
//import com.generic_tools.logger.Logger;
//import org.usb4java.*;
//import org.usb4java.javax.adapter.UsbDeviceAdapter;
//
//import javax.usb.*;
//import javax.usb.event.UsbPipeDataEvent;
//import javax.usb.event.UsbPipeErrorEvent;
//import javax.usb.event.UsbPipeListener;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.PipedInputStream;
//import java.io.PipedOutputStream;
//import java.nio.ByteBuffer;
//import java.nio.IntBuffer;
//import java.nio.file.AccessDeniedException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * TwoWaySerialComm have the ability to send and receive packets using USB serial device.
// *
// * @author taljmars
// *
// */
//public class TwoWaySerialCommNG implements SerialConnection, UsbPipeListener {
//
//	private String PORT_NAME = null;// = "COM9";
//	//private final static int BAUD_RATE = 57600;
//	private int BAUD_RATE;// = 115200;
//
////	private SerialPort serialPort;
//
//	private PipedInputStream fakeIn;
//	private PipedOutputStream fakeOut;
//	private Logger logger;
//
//	private static int called;
//
//	/* Collecting statistics */
//	// RX, TX
//	private long receivedBytes = 0;
//	private long transmittedBytes = 0;
//
//	// Received bps
//	private long lastReadTimestamp = 0;
//	private long bytesSinceLastRead = 0;
//	private long receivedBytesPerSecond = 0;
//
//	// Transmitter bps
//	private long lastWriteTimestamp = 0;
//	private long bytesSinceLastWrite = 0;
//	private long transmittedBytesPerSecond = 0;
//	private Map<String, USBConnector> epMap;
//
//	private UsbPipe usbPipeIn;
//	private UsbPipe usbPipeOut;
//	UsbInterface connection;
//
//	public TwoWaySerialCommNG(Logger logger) {
//		this.logger = logger;
//		this.epMap = new HashMap<>();
//	}
//
//	@Override
//	public void setBaud(Integer boud) {
//		BAUD_RATE = boud;
//	}
//
//	@Override
//	public void setPortName(String port_name) {
//		PORT_NAME = port_name.substring(0, port_name.indexOf(" "));
//	}
//
//	/**
//	 * This function try to connect the default port defined.
//	 */
//	public boolean connect() {
//		logger.LogGeneralMessege("Radio communication manager created");
//
//		try {
//			if (PORT_NAME == null)
//				return false;
//
//			if (!connect(PORT_NAME))
//				return false;
//
//			resetCounters();
//		}
//
//		catch (Exception e) {
//			logger.LogErrorMessege("Unexpected Error:");
//			logger.LogErrorMessege(e.getMessage());
//			return false;
//		}
//
//		logger.LogGeneralMessege("Radio communication manager started successfully");
//
//		return true;
//	}
//
//	/**
//	 * This function try to connect to a specific port
//	 *
//	 * @param portName
//	 * @throws Exception
//	 */
//	byte[] buff;
//	public static final byte USBASP_FUNC_UART_SETBAUDRATE = 53;
//	private boolean connect( String portName ) throws Exception {
//		Context usbContext = new Context();
//		int result = LibUsb.init(usbContext);
//		if (result != LibUsb.SUCCESS) {
//			throw new LibUsbException("Unable to initialize libusb.", result);
//		}
//
//
//		System.out.println("Connecting to " + portName);
//		System.out.println("Connecting to " + epMap.get(portName));
//		connection = epMap.get(portName).usbInterface;
//
//		System.out.println("Connecting to " + connection.getUsbConfiguration().getUsbDevice().getManufacturerString());
//		DeviceHandle usbaspDeviceHandle = LibUsb.openDeviceWithVidPid(usbContext,
//				connection.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idVendor(),
//				connection.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct());
//
//
//		System.out.println("Speed " + connection.getUsbConfiguration().getUsbDevice().getSpeed());
//		if (connection.getUsbEndpoints().size() != 2)
//			return false;
//
//		if (!connection.isActive()) {
//			System.out.println("Not active");
//			return false;
//		}
//
//		connection.claim(usbInterface -> true);
//
//		short param1 = (short) BAUD_RATE;
//		short param2 = (short) (BAUD_RATE >> 16);
//		final ByteBuffer dataBuf = ByteBuffer.allocateDirect(100);
//		result = LibUsb.controlTransfer(
//				usbaspDeviceHandle,
//				(byte) (LibUsb.REQUEST_TYPE_VENDOR | LibUsb.RECIPIENT_DEVICE | LibUsb.ENDPOINT_IN),
//				USBASP_FUNC_UART_SETBAUDRATE,
//				param1,
//				param2,
//				dataBuf,
//				5000);
//
//		System.out.println("Baud was set to " + BAUD_RATE);
//
//
//		System.out.println(((UsbEndpoint) connection.getUsbEndpoints().get(0)).getUsbEndpointDescriptor());
//		System.out.println(((UsbEndpoint) connection.getUsbEndpoints().get(1)).getUsbEndpointDescriptor());
//
//		usbPipeIn = ((UsbEndpoint) connection.getUsbEndpoints().get(1)).getUsbPipe();
//		usbPipeIn.addUsbPipeListener(this);
//		usbPipeIn.open();
//		int size = usbPipeIn.getUsbEndpoint().getUsbEndpointDescriptor().wMaxPacketSize()*100;
//		buff = new byte[size];
//		usbPipeIn.asyncSubmit(buff);
//
//		usbPipeOut = ((UsbEndpoint) connection.getUsbEndpoints().get(0)).getUsbPipe();
//		usbPipeOut.open();
//
//
//
//		fakeOut = new PipedOutputStream();
//		fakeIn = new PipedInputStream(fakeOut, size);
//
//
//		return true;
//	}
//
//	@Override
//	public boolean disconnect() {
//			logger.LogErrorMessege("Disconnected");
//
//			return true;
//
//	}
//
//	/**
//	 * read byte after byte from the USB device
//	 *
//	 * @param readData 	- buffer for reading data
//	 * @param len		- size of the buffer
//	 * @return
//	 */
//	public int read(byte[] readData, int len) {
//		try {
//			int i = 0;
//			int b = '\n';
//
//
////			while ((b = this.in.read()) != '\n' && b != -1) {
//			if (this.fakeIn == null) {
////				logger.LogErrorMessege("In stream is not initialized");
//				return 0;
//			}
////			while ((b = this.fakeIn.read()) != -1) {
//			while (fakeIn.available() > 0) {
//				int bytesread = fakeIn.read(readData, 0, len);
//				if (bytesread < 0)
//					break;
//				i += bytesread;
//				if (i == len) {
////					throw new Exception("Buffer Overflow");
//					logger.LogErrorMessege("Buffer Overflow [exceeded " + len + " bytes]");
//					break;
//				}
////				System.out.println("b = " + b + " i " + i);
////				readData[i++] = (byte) b;
//			}
//
//			// Statistics
//			bytesSinceLastRead += i;
//			receivedBytes += i;
//			if (System.currentTimeMillis() - lastReadTimestamp > 1000) {
//				receivedBytesPerSecond = (long) ((1.0 * bytesSinceLastRead) /
//						((System.currentTimeMillis() - lastReadTimestamp) / 1000));
//				lastReadTimestamp = System.currentTimeMillis();
//				bytesSinceLastRead = 0;
//			}
//
//			return i;
//		}
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
//		catch (Exception e) {
//			logger.LogErrorMessege("Unexpected Error:");
//			logger.LogErrorMessege(e.getMessage());
//			logger.close();
//			e.printStackTrace();
//			System.exit(-6);
//		}
//
//		return -1;
//	}
//
//	/**
//	 * write text byte after byte to the USB device
//	 *
//	 * @param text
//	 */
//	public void write(String text) {
//		if (this.usbPipeOut == null) {
//			logger.LogErrorMessege("Out stream wan't initialed");
//			return;
//		}
//		System.out.println("Sending: " + text);
//		if (text != null && this.usbPipeOut != null)
//			this.write( (text + "\n").getBytes() );
//	}
//
//	DeviceHandle handle = new DeviceHandle();
//	@Override
//	public void write(byte[] buffer) {
//		try {
//
//			int bytesWritten = 0;
//			bytesWritten = usbPipeOut.syncSubmit(buffer);
//
//			// Statistics
//			bytesSinceLastWrite += bytesWritten;
//			transmittedBytes += bytesWritten;
//			if (System.currentTimeMillis() - lastWriteTimestamp > 1000) {
//				transmittedBytesPerSecond = (long) ((1.0 * bytesSinceLastWrite) /
//							((System.currentTimeMillis() - lastWriteTimestamp) / 1000) );
//				lastWriteTimestamp = System.currentTimeMillis();
//				bytesSinceLastWrite = 0;
//			}
//		}
//		catch (UsbException e) {
//			logger.LogErrorMessege("Failed to write messages");
//			logger.LogErrorMessege(e.getMessage());
//			logger.close();
//			e.printStackTrace();
//			System.exit(-1); //For develop purpose TODO: remove it one stabilize
//		}
//	}
//
//	private static ArrayList<USBConnector> dumpDevice(final UsbDevice device)
//	{
//		ArrayList<USBConnector> arrayList = new ArrayList();
//
//		// Dump information about the device itself
////		System.out.println(device);
//		final UsbPort port = device.getParentUsbPort();
//		if (port != null)
//		{
////			System.out.println("Connected to port: " + port.getPortNumber());
////			System.out.println("Parent: " + port.getUsbHub());
//		}
//
//		// Dump device descriptor
////		System.out.println(device.getUsbDeviceDescriptor());
//
//		// Process all configurations
//		for (UsbConfiguration configuration: (List<UsbConfiguration>) device.getUsbConfigurations())
//		{
//			// Dump configuration descriptor
////			System.out.println(configuration.getUsbConfigurationDescriptor());
//
//			// Process all interfaces
//			for (UsbInterface iface: (List<UsbInterface>) configuration.getUsbInterfaces())
//			{
//				if (iface.getUsbInterfaceDescriptor().bInterfaceClass() == 224)
//					continue;
//
//				if (iface.getUsbInterfaceDescriptor().bInterfaceClass() == 14)
//					continue;
//
//				if (iface.getUsbInterfaceDescriptor().bInterfaceClass() == 3)
//					continue;
//
//				if (iface.getUsbInterfaceDescriptor().bInterfaceClass() == 9)
//					continue;
//
//				// Dump the interface descriptor
////				System.out.println(iface.getUsbInterfaceDescriptor());
//
//				arrayList.add(new USBConnector(iface));
//
//				// Process all endpoints
//				for (UsbEndpoint endpoint: (List<UsbEndpoint>) iface.getUsbEndpoints())
//				{
//					// Dump the endpoint descriptor
////					System.out.println("End Point" + endpoint.getUsbEndpointDescriptor());
//
////					arrayList.add(new USBConnector(endpoint));
//				}
//			}
//		}
//
////		System.out.println();
//
//		// Dump child devices if device is a hub
//		if (device.isUsbHub())
//		{
//			final UsbHub hub = (UsbHub) device;
//			for (UsbDevice child: (List<UsbDevice>) hub.getAttachedUsbDevices())
//			{
//				arrayList.addAll(dumpDevice(child));
//			}
//		}
//
//		return arrayList;
//	}
//
//	/**
//	 * get available USB port with devices connected to the machine
//	 *
//	 * @return String array of available ports
//	 */
//	@SuppressWarnings("unchecked")
//	public Object[] listPorts()
//	{
//		final UsbServices services;
//		ArrayList<USBConnector> res = new ArrayList();
//		try {
//			services = UsbHostManager.getUsbServices();
//
//			logger.LogGeneralMessege("USB Service Implementation: " + services.getImpDescription());
//			logger.LogGeneralMessege("Implementation version: " + services.getImpVersion());
//			logger.LogGeneralMessege("Service API version: " + services.getApiVersion());
//
//			res.addAll(dumpDevice(services.getRootUsbHub()));
//			for (USBConnector usbConnector : res) {
//				this.epMap.put(String.valueOf(usbConnector.hashCode()), usbConnector);
//			}
//
//		} catch (UsbException e) {
//			e.printStackTrace();
//		}
//		return res.toArray();
//	}
//
//	/**
//	 * get the port type name
//	 *
//	 * @param portType id
//	 * @return port type name
//	 */
//	private String getPortTypeName ( int portType )
//	{
//
//			return "unknown type";
////		}
//	}
//
//	@Override
//	public Integer[] baudList() {
//		Integer[] oblist = new Integer[]{57600, 115200};// Arrays.asList(57600, 115200).toArray();
//		return oblist;
//	}
//
//	@Override
//	public Integer getDefaultBaud() {
//		return 57600;
//	}
//
//	@Override
//	public long getReceivedBytesPerSeconds() {
//		return receivedBytesPerSecond;
//	}
//
//	@Override
//	public long getTransmittedBytesPerSeconds() {
//		return transmittedBytesPerSecond;
//	}
//
//	@Override
//	public long getTx() {
//		return transmittedBytes;
//	}
//
//	@Override
//	public long getRx() {
//		return receivedBytes;
//	}
//
//	@Override
//	public void resetCounters() {
//		receivedBytes = 0;
//		transmittedBytes = 0;
//
//		// Received bps
//		lastReadTimestamp = 0;
//		bytesSinceLastRead = 0;
//		receivedBytesPerSecond = 0;
//
//		// Transmitter bps
//		lastWriteTimestamp = 0;
//		bytesSinceLastWrite = 0;
//		transmittedBytesPerSecond = 0;
//	}
//
//	@Override
//	public void errorEventOccurred(UsbPipeErrorEvent usbPipeErrorEvent) {
//		System.out.println("GOT Error " + usbPipeErrorEvent.getUsbException().getMessage());
//	}
//
//	@Override
//	public void dataEventOccurred(UsbPipeDataEvent usbPipeDataEvent) {
//		try {
////			System.out.println("GOT " + usbPipeDataEvent.getActualLength());
////			System.out.println(usbPipeDataEvent.getData());
//			fakeOut.write(usbPipeDataEvent.getData());
//			fakeOut.flush();
//			usbPipeIn.asyncSubmit(buff);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}