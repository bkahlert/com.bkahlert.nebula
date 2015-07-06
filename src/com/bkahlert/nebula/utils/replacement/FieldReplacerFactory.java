package com.bkahlert.nebula.utils.replacement;

import java.io.File;

public class FieldReplacerFactory {
	public static IFieldReplacer createFieldReplacer(File file, String encoding) {
		if (file.toString().endsWith(".txt"))
			return new TextFieldReplacer(file, encoding);
		if (file.toString().endsWith(".rtf"))
			return new RtfFieldReplacer(file, encoding);
		if (file.toString().endsWith(".html"))
			return new HtmlFieldReplacer(file, encoding);
		if (file.toString().endsWith(".htm"))
			return new HtmlFieldReplacer(file, encoding);
		return null;
	}
}
