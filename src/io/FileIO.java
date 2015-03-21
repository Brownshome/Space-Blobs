package io;

import io.user.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileIO {

	public static String readFileAsText(String name) {
		String code = "";

		try {
			List<String> lines = Files.readAllLines(Paths.get(name), StandardCharsets.UTF_8);
			for(String line : lines)
				code += line + "\n";
		} catch (IOException e) {
			Console.error(e, "IO");
		}

		return code;
	}
}
