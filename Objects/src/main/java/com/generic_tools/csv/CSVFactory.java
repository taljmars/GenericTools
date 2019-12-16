package com.generic_tools.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class CSVFactory {

    public enum Mode {
        READ,
        WRITE,
        APPEND
    }

    public static CSV open(String path, Mode mode) {
        CSVImpl csvI = null;
        try {
            File csvFile = new File(path);
            File csvDir = csvFile.getParentFile();
            if (!csvDir.exists()) {
                if (mode != Mode.READ)
                    throw new FileNotFoundException("File doesn't exist");

                csvDir.mkdir();
            }

            csvI = new CSVImpl(path);
            switch (mode) {
                case READ:
                    csvI.openRead();
                    break;
                case WRITE:
                    csvI.openWriteNew();
                    break;
                case APPEND:
                    csvI.openWriteAppend();
                    break;
            }

            return csvI;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Failed to open log file, log will not be available");
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.err.println("Failed to open log file, log will not be available");
            return null;
        }
    }

    public static void closeFile(CSV csv) {
        if (csv == null)
            return;
        CSVImpl csv1 = (CSVImpl) csv;
        csv1.close();
    }
}
