package com.generic_tools.csv;

import java.util.Iterator;
import java.util.List;

public interface CSV extends Iterable<List<Object>> {

    List<Object> readEntry();

    CSV addEntry(List<Object> asList);

    CSV addEntries(List<List<Object>> entries);

    CSV addEmptyLine();

    String getFileName();
}
