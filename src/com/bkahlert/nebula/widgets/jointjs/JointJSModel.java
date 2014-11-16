package com.bkahlert.nebula.widgets.jointjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

import com.bkahlert.nebula.utils.JSONUtils;

public class JointJSModel {

	private HashMap<String, Object> json;

	public JointJSModel(String json) {
		this.update(json);
	}

	@SuppressWarnings("unchecked")
	public void update(String json) {
		try {
			this.json = (HashMap<String, Object>) JSONUtils.parseJson(json);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public String getTitle() {
		return (String) this.json.get("title");
	}

	public void setTitle(String title) {
		try {
			Assert.isNotNull(title);
		} catch (AssertionFailedException e) {
			throw new IllegalArgumentException(e);
		}
		this.json.put("title", title);
	}

	@SuppressWarnings("unchecked")
	private List<JointJSCell> getCells() {
		List<JointJSCell> cells = new ArrayList<>();
		for (HashMap<String, Object> cell : (List<HashMap<String, Object>>) this.json
				.get("cells")) {
			cells.add(JointJSCellFactory.createJointJSCell(cell));
		}
		return cells;
	}

	public List<JointJSElement> getElements() {
		return this.getCells().stream()
				.filter(c -> c instanceof JointJSElement)
				.map(c -> (JointJSElement) c).collect(Collectors.toList());
	}

	public List<JointJSLink> getLinks() {
		return this.getCells().stream().filter(c -> c instanceof JointJSLink)
				.map(c -> (JointJSLink) c).collect(Collectors.toList());
	}

	public String serialize() {
		return JSONUtils.buildJson(this.json);
	}

	@Override
	public String toString() {
		return JointJSModel.class.getSimpleName() + " \"" + this.getTitle()
				+ "\": " + this.getElements().size() + " codes, "
				+ this.getLinks().size() + " links";
	}

}
