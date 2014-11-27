package com.bkahlert.nebula.information.extender;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.information.EnhanceableInformationControl;
import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.widgets.composer.Composer;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.nebula.widgets.composer.ComposerReadOnly;
import com.bkahlert.nebula.widgets.editor.Editor;

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

	private final Map<InformationControl<INFORMATION>, ComposerReadOnly> composers = new HashMap<InformationControl<INFORMATION>, ComposerReadOnly>();
	private final Map<InformationControl<INFORMATION>, Editor<INFORMATION>> editors = new HashMap<InformationControl<INFORMATION>, Editor<INFORMATION>>();

	private final GridDataFactory layoutData;

	/**
	 * Creates a new instance.
	 *
	 * @param layoutData
	 *            to be used for the {@link Editor}.
	 */
	public EditorInformationControlExtender(GridDataFactory layoutData) {
		this.layoutData = layoutData;
	}

	@Override
	public void extend(
			final InformationControl<INFORMATION> informationControl,
			Composite parent) {
		this.disposeControls(informationControl);

		if (!this.isEnhanced(informationControl)) {
			ComposerReadOnly composer = new ComposerReadOnly(parent, SWT.NONE);
			composer.setLayoutData(this.layoutData.create());
			this.composers.put(informationControl, composer);
		} else {
			final Editor<INFORMATION> editor = new Editor<INFORMATION>(parent,
					SWT.NONE, 50, ToolbarSet.DEFAULT) {
				@Override
				public String getTitle(INFORMATION objectToLoad,
						IProgressMonitor monitor) throws Exception {
					return EditorInformationControlExtender.this.getTitle(
							objectToLoad, monitor);
				}

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
			editor.setLayoutData(this.layoutData.create());
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
		}
	}

	@Override
	public void extend(InformationControl<INFORMATION> informationControl,
			INFORMATION information) {
		LOGGER.debug("Filling " + this + " with " + information
				+ "\n\t# composers: " + this.composers.size()
				+ "\n\t# editors: " + this.editors.size());
		ComposerReadOnly composer = this.composers.get(informationControl);
		if (composer != null) {
			composer.setSource(this.getHtml(information, null));
		}
		Editor<INFORMATION> editor = this.editors.get(informationControl);
		if (editor != null) {
			editor.load(information);
		}
	}

	/**
	 * Returns the title for the given object.
	 *
	 * @param objectToLoad
	 * @param monitor
	 * @return
	 */
	public abstract String getTitle(INFORMATION objectToLoad,
			IProgressMonitor monitor);

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

	public void disposeControls(
			final InformationControl<INFORMATION> informationControl) {
		if (this.composers.containsKey(informationControl)) {
			Composer composer = this.composers.get(informationControl);
			if (!composer.isDisposed()) {
				composer.dispose();
			}
		}
		if (this.editors.containsKey(informationControl)) {
			Editor<INFORMATION> editor = this.editors.get(informationControl);
			if (!editor.isDisposed()) {
				editor.dispose();
			}
		}
	}

	public boolean isEnhanced(
			final InformationControl<INFORMATION> informationControl) {
		return informationControl instanceof EnhanceableInformationControl ? ((EnhanceableInformationControl<?, ?>) informationControl)
				.isEnhanced() : false;
	}

}
