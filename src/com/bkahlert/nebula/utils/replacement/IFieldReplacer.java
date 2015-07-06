package com.bkahlert.nebula.utils.replacement;

import java.io.File;
import java.util.List;

public interface IFieldReplacer {
	public String getEncoding();

	public String replace(List<IReplacement> replacements)
			throws ReplacementException;

	public void replaceTo(List<IReplacement> replacements, File to)
			throws ReplacementException;

	public void replaceToSelf(List<IReplacement> replacements) throws ReplacementException;
}
