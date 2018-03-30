package com.generic_tools.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class CSVFactory {

    public static CSV createNew(String path) {
        CSVImpl csvI = null;
        try {
            File csvFile = new File(path);
            File csvDir = csvFile.getParentFile();
            if (!csvDir.exists())
                csvDir.mkdir();

            csvI = new CSVImpl(path);
            csvI.open();
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
        CSVImpl csv1 = (CSVImpl) csv;
        csv1.close();
    }
}
