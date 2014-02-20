package com.bkahlert.nebula.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ExecutionUtils {
	/**
	 * Executes the given command and returns its result code.
	 * 
	 * @param command
	 * @param returnValue
	 *            contains the output (content will be overwritten)
	 * @return
	 */
	public static int execute(String command,
			AtomicReference<String> returnValue) {
		int exitValue;
		List<String> output = new ArrayList<String>();
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(new String[] { "/bin/bash", "-c",
					command });
			exitValue = process.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = "";
			while ((line = buf.readLine()) != null) {
				output.add(line);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		returnValue.set(StringUtils.join(output, "\n"));
		return exitValue;
	}

	/**
	 * Executes the given command and returns its result code.
	 * 
	 * @param command
	 * @return
	 */
	public static int execute(String command) {
		int exitValue;
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(new String[] { "/bin/bash", "-c",
					command });
			exitValue = process.waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return exitValue;
	}
}
