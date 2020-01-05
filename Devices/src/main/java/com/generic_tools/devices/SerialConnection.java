package com.generic_tools.devices;

public interface SerialConnection {
	
	boolean connect() throws Exception;
	
	boolean disconnect();
	
	Object[] listPorts();

	void write(String val);

	void write(byte[] buffer);

	int read(byte[] readData, int length);

	void setPortName(String port_name);

	void setBaud(Integer baud);

	Integer[] baudList();

	Integer getDefaultBaud();

	/**
	 * Statistics
	 */

	long getReceivedBytesPerSeconds();

	long getTransmittedBytesPerSeconds();

	long getTx();

	long getRx();

	void resetCounters();
}
