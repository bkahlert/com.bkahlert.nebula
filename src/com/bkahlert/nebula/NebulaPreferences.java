package com.bkahlert.nebula;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;

import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.OSGIPreferenceUtil;

// TODO for some reason (3->4?) does not persist settings
// therefore using newly created OSGIPreferenceUtil
public class NebulaPreferences extends OSGIPreferenceUtil {

	private static final Logger LOGGER = Logger
			.getLogger(NebulaPreferences.class);

	public NebulaPreferences() {
		super(Activator.getDefault().getBundle().getBundleContext());
	}

	private <SRC, DST> DST convert(SRC value,
			IConverter<SRC, DST>... converters) {
		for (IConverter<SRC, DST> converter : converters) {
			DST converted = converter.convert(value);
			if (converted != null) {
				return converted;
			}
		}
		return null;
	}

	/**
	 * Saves the expanded elements of the given {@link TreeViewer} under the
	 * given key. The key does not conflict with eventually other methods that
	 * use a key to identify some resource.
	 * 
	 * @param key
	 * @param treeViewer
	 */
	public void saveExpandedElements(String key, TreeViewer treeViewer,
			IConverter<Object, String>... converters) {
		Object[] expandedElements = treeViewer.getExpandedElements();

		List<String> serializedElements = new ArrayList<String>();
		for (Object expandedElement : expandedElements) {
			String serializedElement = this
					.convert(expandedElement, converters);
			if (serializedElement != null) {
				serializedElements.add(serializedElement);
			}
		}
		this.getSystemPreferences().putByteArray(
				"expandedElements." + key,
				SerializationUtils.serialize(serializedElements
						.toArray(new String[0])));
	}

	/**
	 * Restores the expanded elements of the given {@link TreeViewer} previously
	 * saved through {@link #saveExpandedElements(String, TreeViewer)}
	 * 
	 * @param key
	 * @param treeViewer
	 */
	public void loadExpandedElements(String key, TreeViewer treeViewer,
			IConverter<String, Object>... converters) {
		try {
			byte[] stored = this.getSystemPreferences().getByteArray(
					"expandedElements." + key, null);
			String[] serializedElements = (String[]) SerializationUtils
					.deserialize(stored);
			List<Object> expandedElements = new LinkedList<Object>();
			for (String serializedElement : serializedElements) {
				Object expandedElement = this.convert(serializedElement,
						converters);
				if (expandedElement != null) {
					expandedElements.add(expandedElement);
				}
			}
			for (Object expandedElement : expandedElements) {
				if (expandedElement != null) {
					treeViewer.expandToLevel(expandedElement, 1);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error loading expanded elements of " + treeViewer);
		}
	}

}
