package com.bkahlert.devel.nebula.utils;

import java.util.List;

public class StringUtils {

	public static String join(List<String> strings, String separator) {
		if (strings == null)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0, m = strings.size(); i < m; i++) {
			String string = strings.get(i);
			if (string == null)
				string = "";

			sb.append(string);
			if (i + 1 < m)
				sb.append(separator);
		}
		return sb.toString();
	}

}
