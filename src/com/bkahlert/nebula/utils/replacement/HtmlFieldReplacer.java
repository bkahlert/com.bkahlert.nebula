package com.bkahlert.nebula.utils.replacement;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class HtmlFieldReplacer extends TextFieldReplacer {

	public HtmlFieldReplacer(File file, String encoding) {
		super(file, encoding != null ? encoding : "windows-1252");
	}

	@Override
	protected String fuseReplacements(List<String> strings) {
		return StringUtils.join(strings, "<br/>");
	}

	@Override
	public void replaceTo(List<IReplacement> replacements, final File to)
			throws ReplacementException {
		super.replaceTo(replacements, to);

		// also copy the asset directory
		for (File srcDir : this.file.getParentFile().listFiles(
				new FilenameFilter() {
					public boolean accept(File dir, String name) {
						File file = new File(dir, name);
						// Folder names are not always deterministic
						return file.isDirectory();
					}
				})) {
			try {
				File destDir = new File(to.getParentFile(),
						FilenameUtils.getName(srcDir.toString()));
				FileUtils.copyDirectory(srcDir, destDir);
			} catch (IOException e) {
				throw new ReplacementException(e);
			}
		}
	}

}
