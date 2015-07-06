package com.bkahlert.nebula.utils.replacement;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class RtfFieldReplacer extends TextFieldReplacer {

	public RtfFieldReplacer(File file, String encoding) {
		super(file, encoding != null ? encoding : "windows-1252");
	}

	@Override
	protected String fuseReplacements(List<String> strings) {
		return StringUtils.join(strings, "\\\\par ");
	}

	protected String getKeyPattern(String key) {
		return "\\$\\\\\\{" + key + "\\\\}";
	}

}
