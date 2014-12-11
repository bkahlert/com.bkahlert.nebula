package com.bkahlert.nebula;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.OSGIPreferenceUtil;
import com.bkahlert.nebula.utils.SerializationUtils;

// TODO for some reason (3->4?) does not persist settings
// therefore using newly created OSGIPreferenceUtil
public class NebulaPreferences extends OSGIPreferenceUtil {

	private static final Logger LOGGER = Logger
			.getLogger(NebulaPreferences.class);

	public NebulaPreferences() {
		super(Activator.getDefault().getBundle().getBundleContext());
	}

	private <SRC, DST> DST convert(SRC value,
			@SuppressWarnings("unchecked") IConverter<SRC, DST>... converters) {
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
	public void saveExpandedElements(
			String key,
			TreeViewer treeViewer,
			@SuppressWarnings("unchecked") IConverter<Object, String>... converters) {
		TreePath[] treePaths = treeViewer.getExpandedTreePaths();

		List<List<String>> serializedTreePaths = new ArrayList<>();
		for (TreePath treePath : treePaths) {
			List<String> serializedTreePath = new ArrayList<>();
			for (int i = 0; i < treePath.getSegmentCount(); i++) {
				String serializedElement = this.convert(treePath.getSegment(i),
						converters);
				if (serializedElement == null) {
					break;
				}
				serializedTreePath.add(serializedElement);
			}
			serializedTreePaths.add(serializedTreePath);
		}

		this.getSystemPreferences().put(
				"expandedElements." + key,
				SerializationUtils.serialize(serializedTreePaths,
						list -> SerializationUtils.serialize(list,
								string -> string)));
	}

	/**
	 * Restores the expanded elements of the given {@link TreeViewer} previously
	 * saved through {@link #saveExpandedElements(String, TreeViewer)}
	 *
	 * @param key
	 * @param treeViewer
	 */
	public void loadExpandedElements(
			String key,
			final TreeViewer treeViewer,
			@SuppressWarnings("unchecked") IConverter<String, Object>... converters) {
		try {
			String stored = this.getSystemPreferences().get(
					"expandedElements." + key, null);
			@SuppressWarnings("unchecked")
			List<String> serializedTreePaths = SerializationUtils.deserialize(
					stored, List.class);
			final List<TreePath> treePaths = new LinkedList<>();
			for (String serializedTreePath : serializedTreePaths) {
				List<Object> treePath = new LinkedList<>();
				for (Object serializedElement : SerializationUtils.deserialize(
						serializedTreePath, List.class)) {
					Object expandedElement = this.convert(
							(String) serializedElement, converters);
					if (expandedElement != null) {
						treePath.add(expandedElement);
					}
				}
				treePaths.add(new TreePath(treePath.toArray()));
			}
			ExecUtils.syncExec(() -> {
				treeViewer.setExpandedTreePaths(treePaths
						.toArray(new TreePath[0]));
			});
		} catch (Exception e) {
			LOGGER.error("Error loading expanded elements of " + treeViewer, e);
		}
	}

}
