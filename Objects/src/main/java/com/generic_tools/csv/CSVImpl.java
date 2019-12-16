package com.generic_tools.csv;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSVImpl implements CSV {
	
	private final String CSV_SEPARATOR = ",";
	private final String fileName;
	private PrintWriter writer = null;
	BufferedReader bufferedReader = null;

	public CSVImpl(String file_full_path) {
		if (!file_full_path.endsWith(".csv"))
			fileName = file_full_path + ".csv";
		else
			fileName = file_full_path;
	}

	public void openWriteNew() throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(fileName, "UTF-8");
		bufferedReader = new BufferedReader(new FileReader(fileName));
		writer.flush();
	}

	public void openWriteAppend() throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(fileName, "UTF-8");
		bufferedReader = new BufferedReader(new FileReader(fileName));
	}

	public void openRead() throws FileNotFoundException, UnsupportedEncodingException {
		bufferedReader = new BufferedReader(new FileReader(fileName));
	}

	public void close(){
		try {
			if (writer != null)
				writer.close();

			if (bufferedReader!= null)
				bufferedReader.close();
		}
		catch (IOException e) {
		}
	}

	@Override
	public List<Object> readEntry() {
		List<Object> objectList = new ArrayList<>();
		try {
			String line = bufferedReader.readLine();
			Pattern pattern = Pattern.compile("(?:\\\\s*(?:\\\\\\\"([^\\\\\\\"]*)\\\\\\\"|([^,]+))\\\\s*,?)+?");
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				if (matcher.group(1) == null) {
//					System.out.println(matcher.group(2));
					objectList.add(matcher.group(2));
				} else {
//					System.out.println(matcher.group(1));
					objectList.add(matcher.group(1));
				}
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
		return objectList;
	}

	@Override
	public CSV addEntry(List<Object> entryValue) {
		if (writer == null)
			throw new RuntimeException("File is not open in writing mode");

		for (int i = 0 ; i < entryValue.size() - 1 ; i++) {
			Object o = entryValue.get(i).toString();
			if (((String) o).contains(CSV_SEPARATOR))
				writer.print("\"" + o + "\"" + CSV_SEPARATOR);
			else
				writer.print(o + CSV_SEPARATOR);
		}
		
		writer.println(entryValue.get(entryValue.size() - 1));
		return this;
	}

	@Override
	public CSV addEntries(List<List<Object>> entries) {
		if (writer == null)
			throw new RuntimeException("File is not open in writing mode");

		for (List<Object> lst : entries) {
			for (int i = 0; i < lst.size() - 1; i++) {
				Object o = lst.get(i).toString();
				if (((String) o).contains(CSV_SEPARATOR))
					writer.print("\"" + o + "\"" + CSV_SEPARATOR);
				else
					writer.print(o + CSV_SEPARATOR);
			}
			writer.println(lst.get(lst.size() - 1));
		}

		return this;
	}

	@Override
	public CSV addEmptyLine() {
		if (writer == null)
			throw new RuntimeException("File is not open in writing mode");

		writer.println();
		return this;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return new CSVIterator(bufferedReader);
	}
}
