package com.generic_tools.devices;

public interface SerialConnection {
	
	public boolean connect() throws Exception;
	
	public boolean disconnect();
	
	public Object[] listPorts();

	public void write(String val);

	public void write(byte[] buffer);

	public int read(byte[] readData, int length);

	public void setPortName(String port_name);

	public void setBaud(Integer baud);

	public Integer[] baudList();

	public Integer getDefaultBaud();

	/**
	 * Statistics
	 */

	public long getReceivedBytesPerSeconds();

	public long getTransmittedBytesPerSeconds();

	public long getTx();

	public long getRx();
}
