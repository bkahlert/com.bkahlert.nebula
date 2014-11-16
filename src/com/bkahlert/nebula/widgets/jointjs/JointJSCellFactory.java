package com.bkahlert.nebula.widgets.jointjs;

import java.io.IOException;
import java.util.HashMap;

import com.bkahlert.nebula.utils.JSONUtils;

public class JointJSCellFactory {

	public static JointJSCell createJointJSCell(String json) {
		try {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> cell = (HashMap<String, Object>) JSONUtils
					.parseJson(json);
			if (cell.get("type").toString().contains("link")) {
				return new JointJSLink(cell);
			} else {
				return new JointJSElement(cell);
			}
		} catch (ClassCastException | IOException | NullPointerException e) {
			throw new RuntimeException(e);
		}
	}

}
