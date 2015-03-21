package time;

import io.user.KeyIO;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;

public class Profiler {
	static HashMap<String, Long> times =  new HashMap<>();
	static String current = "";
	static final float RES = 1000000;
	static long last = Clock.getABSTime(RES);
	static long start = Clock.getABSTime(RES);

	static {
		KeyIO.addAction(Profiler::reset, Keyboard.KEY_R, KeyIO.KEY_PRESSED);
		times.put("", 0l);
	}

	public static void start(String name) {
		set(current + (current.isEmpty() ? "" : ".") + name);
	}

	public static void set(String fullName) {
		times.replaceAll((s, l) -> l += s != "" && current.startsWith(s) || s == "" && current == "" ? Clock.getABSTime(RES) - last : 0); //loving these lambda expressions :D
		current = fullName;
		times.putIfAbsent(fullName, 0l);
		last = Clock.getABSTime(RES);
	}

	public static String get() {
		return current;
	}

	public static void end() {
		if(current.contains("."))
			set(current.substring(0, current.lastIndexOf('.')));
		else
			set("");
	}

	public static void end(String name) {
		set(current.substring(0, current.lastIndexOf(name)));
	}

	public static Map<String, Float> getTimes(String catagory) {
		Map<String, Float> p = new HashMap<>();
		long total = 0;
		for(Entry<String, Long> e : times.entrySet()) {
			String c = e.getKey();
			if(c.split("[.]").length == catagory.split("[.]").length + (catagory.isEmpty() ? 0 : 1) && c.startsWith(catagory) && !c.isEmpty()) {
				p.put(e.getKey(), (float) e.getValue());
				total += e.getValue();
			}
		}

		final float t = total;
		p.replaceAll((s, f) -> f / t);
		return p;
	}

	public static Map<String, Float> getTimes() {
		return getTimes("");
	}

	public static void reset() {
		last = start = Clock.getABSTime(RES);
		current = "";
		times.clear();
		times.put("", 0l);
	}
}
