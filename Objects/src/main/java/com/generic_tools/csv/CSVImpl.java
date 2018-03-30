package com.generic_tools.csv;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class CSVImpl implements CSV {
	
	private final String CSV_SEPARATOR = ",";
	private final String fileName;
	private PrintWriter writer = null;

	public CSVImpl(String file_full_path) {
		if (!file_full_path.endsWith(".csv"))
			fileName = file_full_path + ".csv";
		else
			fileName = file_full_path;
	}

	public void open() throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(fileName, "UTF-8");
	}

	public void close(){
		if (writer != null)
			writer.close();
	}

	@Override
	public CSV addEntry(List<Object> entryValue) {
		for (int i = 0 ; i < entryValue.size() - 1 ; i++) {
			writer.print(entryValue.get(i).toString() + CSV_SEPARATOR);
		}
		
		writer.println(entryValue.get(entryValue.size() - 1));
		return this;
	}

	@Override
	public CSV addEntries(List<List<Object>> entries) {
		for (List<Object> lst : entries) {
			for (int i = 0; i < lst.size() - 1; i++) {
				writer.print(lst.get(i).toString() + CSV_SEPARATOR);
			}
			writer.println(lst.get(lst.size() - 1));
		}

		return this;
	}

	@Override
	public CSV addEmptyLine() {
		writer.println();
		return this;
	}

	@Override
	public String getFileName() {
		return fileName;
	}
}
