package com.generic_tools.environment;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by taljmars on 3/4/17.
 */
public class Environment {

    private static final String LOG_MAIN_DIRECTORY = "logs";
    private static final String LOG_ENTRY_PREFIX = "quadlog_";

    private static final String CACHE_MAIN_DIRECTORY = "cache";

    private static final String CONF_MAIN_DIRECTORY = "conf";

    public static final String DIR_SEPERATOR = "//";

    private static Date dateTimestemp = new Date();

    private String externalBaseDirectory;

    public Environment() {
    }

    public Environment(String externalBaseDirectory) {
        this.externalBaseDirectory = externalBaseDirectory;
    }

    public File getRunningEnvLogDirectory() throws URISyntaxException {
        File file = getRunningEnvBaseDirectory();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM_dd_hhmmss");
        String dateAsString = simpleDateFormat.format(dateTimestemp);
        file = new File(file.toString() + DIR_SEPERATOR + LOG_MAIN_DIRECTORY + DIR_SEPERATOR + LOG_ENTRY_PREFIX + dateAsString);
        if (!file.exists())
            file.mkdirs();

        return file;
    }

    public File getRunningEnvConfDirectory() throws URISyntaxException {

        File file = getRunningEnvBaseDirectory();
        file = new File(file.toString() + DIR_SEPERATOR + CONF_MAIN_DIRECTORY);
        if (!file.exists())
            file.mkdirs();

        return file;
    }

    public File getRunningEnvCacheDirectory() throws URISyntaxException {

        File file = getRunningEnvBaseDirectory();
        file = new File(file.toString() + DIR_SEPERATOR + CACHE_MAIN_DIRECTORY);
        if (!file.exists())
            file.mkdirs();

        return file;
    }

    public File getRunningEnvBaseDirectory() throws URISyntaxException {
        if (externalBaseDirectory != null && !externalBaseDirectory.isEmpty())
            return new File(externalBaseDirectory);

        setBaseRunningDirectoryByClass();
        File file = new File(externalBaseDirectory);
        if (!file.exists())
            throw new RuntimeException("Running directory wasn't found");

        return file;
    }

    public void setBaseRunningDirectoryByClass() throws URISyntaxException {
        setBaseRunningDirectoryByClass("UntitledEnvironment");
    }

    public void setBaseRunningDirectoryByClass(String title) throws URISyntaxException {
        File file = new File(System.getProperty("user.dir") + DIR_SEPERATOR + title + DIR_SEPERATOR);
        file.mkdir();
        externalBaseDirectory = file.toString();
    }
}
