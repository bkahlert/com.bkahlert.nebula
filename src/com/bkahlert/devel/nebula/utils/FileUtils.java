package com.bkahlert.devel.nebula.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class FileUtils {
	public static void showFileInFilesystem(File file)
			throws InterruptedException, IOException {
		Assert.isNotNull(file);
		String osName = System.getProperty("os.name");
		if (osName.contains("Mac OS X")) {
			List<String> args = new ArrayList<String>();
			args.add("osascript");
			args.add("-e");
			args.add("tell application \"Finder\"");
			args.add("-e");
			args.add("activate");
			args.add("-e");
			args.add("reveal POSIX file \"" + file.getAbsolutePath() + "\"");
			args.add("-e");
			args.add("end tell");
			Runtime.getRuntime().exec(args.toArray(new String[0])).waitFor();
		} else {
			throw new UnsupportedOperationException(
					"Opening files in your OS is not supported.");
		}
	}
}
