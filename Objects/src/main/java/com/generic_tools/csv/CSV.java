package com.generic_tools.csv;

import java.util.List;

public interface CSV {

	CSV addEntry(List<Object> asList);

    CSV addEntries(List<List<Object>> entries);

    CSV addEmptyLine();

	String getFileName();
}
