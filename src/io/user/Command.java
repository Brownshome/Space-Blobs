package io.user;

import java.util.HashMap;
import java.util.Map;

public interface Command {
	static Map<String, Command> commands = new HashMap<>();

	public static void innit() {

	}

	public static Command DEFAULT_COMMAND = null; //TODO this... = args -> TextInterface.addLine("Command " + args[0] + " is not recognized.");

	public static void dispatch(String command) {
		String[] args = command.split(" ");
		try {
			commands.getOrDefault(args[0], DEFAULT_COMMAND).call(args);
		} catch(Exception e) {
			Console.warn("Command " + args[0] + " failed: " + e, "COMMAND");
		}
	}

	public static boolean exists(String name) {
		return commands.containsKey(name);
	}

	public static void regesterCommand(Command command, String... names) {
		for(String name : names)
			regesterCommand(command, name);
	}

	public static void regesterCommand(Command command, String name) {
		if(commands.containsKey(name))
			Console.error("Command: " + command + " is already regestered", "COMMAND");

		commands.put(name, command);
	}

	/** The first argument will be the name through which the function was called. */
	public void call(String... args);
}
