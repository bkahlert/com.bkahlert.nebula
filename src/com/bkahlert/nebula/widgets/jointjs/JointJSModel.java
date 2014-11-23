package com.bkahlert.nebula.widgets.jointjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.ObjectUtils;

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

	public Object getAttribute(String key) {
		return this.json.get(key);
	}

	public String getTitle() {
		return (String) this.json.get("title");
	}

	public void setTitle(String title) {
		this.json.put("title", title);
	}

	@SuppressWarnings("unchecked")
	public List<JointJSCell> getCells() {
		List<JointJSCell> cells = new ArrayList<>();
		for (HashMap<String, Object> cell : (List<HashMap<String, Object>>) this.json
				.get("cells")) {
			cells.add(JointJSCellFactory.createJointJSCell(cell));
		}
		return cells;
	}

	public JointJSCell getCell(String id) {
		List<JointJSCell> cells = this.getCells().stream()
				.filter(cell -> ObjectUtils.equals(id, cell.getId()))
				.collect(Collectors.toList());
		return cells.size() > 0 ? cells.get(0) : null;
	}

	public List<JointJSElement> getElements() {
		return this.getCells().stream()
				.filter(c -> c instanceof JointJSElement)
				.map(c -> (JointJSElement) c).collect(Collectors.toList());
	}

	public JointJSElement getElement(String id) {
		JointJSCell cell = this.getCell(id);
		return cell instanceof JointJSElement ? (JointJSElement) cell : null;
	}

	public List<JointJSLink> getPermanentLinks() {
		return this
				.getCells()
				.stream()
				.filter(c -> c instanceof JointJSLink
						&& ((JointJSLink) c).isPermanent())
				.map(c -> (JointJSLink) c).collect(Collectors.toList());
	}

	public JointJSLink getPermanentLink(String id) {
		JointJSCell cell = this.getCell(id);
		return cell instanceof JointJSLink
				&& ((JointJSLink) cell).isPermanent() ? (JointJSLink) cell
				: null;
	}

	public List<JointJSLink> getLinks() {
		return this
				.getCells()
				.stream()
				.filter(c -> c instanceof JointJSLink
						&& !((JointJSLink) c).isPermanent())
				.map(c -> (JointJSLink) c).collect(Collectors.toList());
	}

	public JointJSLink getLink(String id) {
		JointJSCell cell = this.getCell(id);
		return cell instanceof JointJSLink
				&& !((JointJSLink) cell).isPermanent() ? (JointJSLink) cell
				: null;
	}

	/**
	 * Create a new {@link JointJSModel} based on this {@link JointJSModel} but
	 * overrides the given fields.
	 *
	 * @param copy
	 * @param customize
	 */
	public JointJSModel createCopy(Map<String, Object> customize) {
		JointJSModel copy = new JointJSModel(this.serialize());
		for (Entry<String, Object> entry : customize.entrySet()) {
			copy.json.put(entry.getKey(), entry.getValue());
		}
		return copy;
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
