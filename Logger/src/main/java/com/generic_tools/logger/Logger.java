package com.generic_tools.logger;

import com.generic_tools.environment.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Date;

public class Logger {
	
	private final String LOG_ENTRY_SUFFIX = ".html";
	
	private PrintWriter writer = null;

	private Environment environment;
	private Level consoleLevel = Level.INFO;
	private Level level = Level.INFO;

	public Logger(Environment environment) {
		this.environment = environment;
		init();
	}

	private static int called;
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");

		try {
			//File logDir = Environment.getRunningEnvLogDirectory();
			File logDir = environment.getRunningEnvLogDirectory();
			writer = new PrintWriter(logDir + Environment.DIR_SEPERATOR + "log" + LOG_ENTRY_SUFFIX, "UTF-8");
			System.out.println(logDir + Environment.DIR_SEPERATOR + "log" + LOG_ENTRY_SUFFIX);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(getClass().getName() + " Failed to open log file, log will not be available");
			writer = null;
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			writer = null;
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			writer = null;
			return;
		}

		Timestamp ts = new Timestamp(new Date().getTime());

		writer.println("<html>");
		writer.println("<title>");
		writer.println("QuadCopter Logbook");
		writer.println("</title>");
		writer.println("<body>");
		writer.println("<h1>QuadCopter Flight Record - " + ts.toString() + "</h1>");
		writer.println("<h3>Legend:</h3>");
		writer.println("<font color=\"black\">Black\t - General messeges</font><br/>");
		writer.println("<font color=\"green\">Green\t - Quadcopter messeges</font><br/>");
		writer.println("<font color=\"blue\">Blue\t  - GroundStation messeges</font><br/>");
		writer.println("<h1></h1>");
		writer.println("<h3>Log:</h3>");
	}

	public enum Level {
		ERROR,
		WARNING,
		DEBUG,
		INFO,
		OFF
	}

	public void setConsoleLevel(Level level) {
		this.consoleLevel = level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	int recordNumber = 0;
	private void log(String str){
		if (level.equals(Level.OFF))
			return;

		if (writer == null)
			return;
		
		writer.println(str);
		recordNumber++;
	}

	public void LogDesignedMessege(String msg) {
		LogDesignedMessege("%s", msg);
	}
	
	public void LogDesignedMessege(String frmt, Object... args) {
		String msg = frmt;
		if (args != null && args.length != 0)
			msg = String.format(frmt, args);
		log(msg);
	}

	public void LogGeneralMessege(String msg) {
		LogGeneralMessege("%s", msg);
	}
	
	public void LogGeneralMessege(String frmt, Object... args) {
		if (level.equals(Level.OFF))
			return;

		String msg = frmt;
		if (args != null && args.length != 0)
			msg = String.format(frmt, args);
		PrintConsole(msg);
		Date date = new Date();
		Timestamp ts = new Timestamp(date.getTime());
		
		String modmsg = "<font color=\"black\">" + "[" + ts.toString() + "] " + msg + "</font>" + "<br/>";
		log(modmsg);
	}

	private void PrintConsole(String msg) {
		if (consoleLevel.equals(Level.OFF))
			return;

		if (consoleLevel.compareTo(Level.DEBUG) >= 0)
			System.out.println(msg);
	}

	public void LogErrorMessege(String msg) {
		LogErrorMessege("%s", msg);
	}
	
	public void LogErrorMessege(String frmt, Object... args) {
		if (level.equals(Level.OFF))
			return;

		String msg = frmt;
		if (args != null && args.length != 0)
			msg = String.format(frmt, args);
		PrintConsole(msg);
		Date date = new Date();
		Timestamp ts = new Timestamp(date.getTime());
		
		String modmsg = "<font color=\"red\">" + "[" + ts.toString() + "] " + msg + "</font>" + "<br/>";
		log(modmsg);
	}
	
	protected void finalize() throws Throwable {
		try {
			System.out.println("Finalize of Sub Class");
	        if (writer != null) {
	        	writer.println("<h4>Total - " + recordNumber + " Records</h4>");
	        	writer.println("</body>");
	    		writer.println("</html>");
	    		writer.close();
	        }
	     } finally {
	         super.finalize();
	     }
    }
	
	public void close() {
		try {
			finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public void LogIncomingMessage(String message) {
		System.err.println(message);
    }

	public void LogOutgoingMessage(String message) {
		System.err.println(message);
	}

	public void LogAlertMessage(String message) {
		System.err.println(message);

	}

	public void LogAlertMessage(String message, Exception e) {
		System.err.println(message);

	}

	public static enum Type {
		GENERAL,
		SUCCESS,
		ERROR,
		WARNING,
		INCOMING,
		OUTGOING
	};
	
	public static String generateDesignedMessege(String cmd, Type t, boolean no_date)
	{
		String newcontent = "";
		
		String ts_string = "";
		if (!no_date) { 
			Date date = new Date();
			Timestamp ts = new Timestamp(date.getTime());
			ts_string = "[" + ts.toString() + "]";
		}
		/*
		 * Currently i am converting NL char to space and comma sep.
		 */
		cmd = cmd.replace("\n", ",");
		cmd = cmd.trim();
		String[] lines = cmd.split("\n");
		for (int i = 0 ; i < lines.length ; i++ ){
			if (lines[i].length() == 0)
				continue;

			switch (t) {
				case GENERAL:
					newcontent = ("<font color=\"black\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case SUCCESS:
					newcontent = ("<font color=\"green\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case OUTGOING:
					newcontent = ("<font color=\"blue\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case INCOMING:
					newcontent = ("<font color=\"green\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case ERROR:
					newcontent = ("<font color=\"red\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case WARNING:
					newcontent = ("<font color=\"orange\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				default:
					newcontent = ("<font color=\"red\">" + ts_string + " Unrecognized: " + lines[i] + "</font>" + "<br/>");
					break;
			}
		}
		
		return newcontent;
	}
}
