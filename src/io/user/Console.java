package io.user;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Console {
	private static int level = 0;
	private static String format = "HH:mm:ss";

	public synchronized static void error(Throwable t, String name) {
		if(level > 3) return;
		System.out.println(getFormatString("ERROR", name) + "An error has occured: " + t.getMessage());
		crash(t);
	}

	public synchronized static void error(String string, Throwable t, String name) {
		if(level > 3) return;
		System.out.println(getFormatString("ERROR", name) + string);
		crash(t);
	}

	public synchronized static void error(String str, String name) {
		if(level > 3) return;
		System.out.println(getFormatString("ERROR", name) + str);
		crash(new RuntimeException("Error"));
	}

	public synchronized static void warn(String str, String name) {
		if(level > 2) return;
		System.out.println(getFormatString("WARNING", name) + str);
	}

	public synchronized static void inform(String str, String name) {
		if(level > 1) return;
		System.out.println(getFormatString("INFO", name) + str);
	}

	public synchronized static void fine(String str, String name) {
		if(level > 0) return;
		System.out.println(getFormatString("FINE", name) + str);
	}

	private static String getFormatString(String level, String name) {
		return "[" + new SimpleDateFormat(format).format(new Date()) + "][" + name + "][" + level + "]";
	}

	/**Please use Console.error(Throwable t, String name) instead */
	public synchronized static void report(Throwable ex) {
		System.out.println("****************** STACK TRACE ******************");
		ex.printStackTrace(System.out);
		System.out.println("****************** TRACE END ******************");
	}

	public synchronized static void crash(Throwable ex) {
		if(level < 4) report(ex);
		System.exit(1);
	}
}
