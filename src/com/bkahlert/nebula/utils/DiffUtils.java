package com.bkahlert.nebula.utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.bkahlert.devel.nebula.utils.FileUtils;

public class DiffUtils {
	public static class PatchFailedException extends Exception {
		private static final long serialVersionUID = 1L;

		public PatchFailedException(String patchOutput) {
			super(patchOutput);
		}

		public PatchFailedException(Throwable innerException) {
			super(innerException);
		}
	}

	public static String patch(String source, String patch)
			throws PatchFailedException {
		try {
			File sourceFile = FileUtils.getTempFile();
			org.apache.commons.io.FileUtils.write(sourceFile, source);

			File patchFile = FileUtils.getTempFile();
			org.apache.commons.io.FileUtils.write(patchFile, patch);

			AtomicReference<String> output = new AtomicReference<String>();
			int exitValue = ExecutionUtils.execute(
					"patch --verbose " + sourceFile.getAbsolutePath() + " < "
							+ patchFile.getAbsolutePath(), output);
			if (exitValue != 0) {
				throw new PatchFailedException(output.get());
			}

			return org.apache.commons.io.FileUtils.readFileToString(sourceFile);
		} catch (IOException e) {
			throw new PatchFailedException(e);
		}
	}
}
