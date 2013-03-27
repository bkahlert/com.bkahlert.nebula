package com.bkahlert.nebula.information.extender;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.editor.Editor;
import com.bkahlert.nebula.information.InformationControl;

/**
 * Instances of this class extends a {@link InformationControl} by a
 * {@link Editor}.
 * 
 * @author bkahlert
 * 
 * @param <INFORMATION>
 */
public abstract class EditorInformationControlExtender<INFORMATION> implements
		IInformationControlExtender<INFORMATION> {

	private static final Logger LOGGER = Logger
			.getLogger(EditorInformationControlExtender.class);

	private Map<InformationControl<INFORMATION>, Editor<INFORMATION>> editors = new HashMap<InformationControl<INFORMATION>, Editor<INFORMATION>>();

	private Object layoutData;

	/**
	 * Creates a new instance.
	 * 
	 * @param layoutData
	 *            to be used for the {@link Editor}.
	 */
	public EditorInformationControlExtender(Object layoutData) {
		this.layoutData = layoutData;
	}

	@Override
	public void extend(
			final InformationControl<INFORMATION> informationControl,
			Composite parent) {
		if (this.editors.containsKey(informationControl)) {
			Editor<INFORMATION> editor = this.editors.get(informationControl);
			if (!editor.isDisposed()) {
				editor.dispose();
			}
		}
		final Editor<INFORMATION> editor = new Editor<INFORMATION>(parent,
				SWT.NONE, 50, ToolbarSet.DEFAULT) {
			@Override
			public String getHtml(INFORMATION objectToLoad,
					IProgressMonitor monitor) {
				return EditorInformationControlExtender.this.getHtml(
						objectToLoad, monitor);
			}

			@Override
			public void setHtml(INFORMATION loadedObject, String html,
					IProgressMonitor monitor) {
				EditorInformationControlExtender.this.setHtml(loadedObject,
						html, monitor);
			}
		};
		editor.setLayoutData(this.layoutData);
		editor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					editor.save();
				} catch (Exception e1) {
					LOGGER.error("Error while saving "
							+ Editor.class.getSimpleName());
				}
			}
		});
		this.editors.put(informationControl, editor);
	};

	@Override
	public void extend(InformationControl<INFORMATION> informationControl,
			INFORMATION information) {
		Editor<INFORMATION> editor = this.editors.get(informationControl);
		if (editor == null) {
			return;
		}
		editor.load(information);
	}

	/**
	 * Returns the html for the given object.
	 * 
	 * @param objectToLoad
	 * @param monitor
	 * @return
	 */
	public abstract String getHtml(INFORMATION objectToLoad,
			IProgressMonitor monitor);

	/**
	 * Sets the given html to the loaded object.
	 * 
	 * @param loadedObject
	 * @param html
	 * @param monitor
	 */
	public abstract void setHtml(INFORMATION loadedObject, String html,
			IProgressMonitor monitor);

}
