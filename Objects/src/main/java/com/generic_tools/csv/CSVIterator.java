package com.generic_tools.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSVIterator implements Iterator<List<Object>> {

    private final BufferedReader bufferedReader;
    private List<Object> nextObject = null;

    public CSVIterator(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
        this.nextObject = readLine();
    }

    @Override
    public boolean hasNext() {
        return nextObject != null;
    }

    @Override
    public List<Object> next() {
        if (this.nextObject == null) {
            throw new NoSuchElementException("End of file");
        }
        List<Object> tmp = this.nextObject;
        this.nextObject = readLine();
        return tmp;
    }

    private List<Object> readLine() {
        List<Object> objectList = new ArrayList<>();
        try {
            String line = bufferedReader.readLine();
            Pattern pattern = Pattern.compile("\\s*(?:\"[^\"]*\"|(?:^|(?<=,))[^,]*)");
//            Pattern pattern = Pattern.compile("(?:\\\\s*(?:\\\\\\\"([^\\\\\\\"]*)\\\\\\\"|([^,]+))\\\\s*,?)+?");
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

            return objectList;
        } catch (IOException e) {
        }
        return null;
    }
}
