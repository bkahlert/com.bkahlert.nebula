package com.bkahlert.nebula.widgets.jointjs;

import java.io.IOException;
import java.util.HashMap;

import com.bkahlert.nebula.utils.JSONUtils;

public class JointJSCellFactory {

	public static JointJSCell createJointJSCell(String json) {
		try {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>) JSONUtils
					.parseJson(json);
			return createJointJSCell(map);
		} catch (ClassCastException | IOException | NullPointerException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static JointJSCell createJointJSCell(HashMap<String, Object> map) {
		try {
			if (map.get("type").toString().contains("link")) {
				return new JointJSLink(map);
			} else {
				return new JointJSElement(map);
			}
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
