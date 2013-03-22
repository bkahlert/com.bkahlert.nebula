package com.bkahlert.nebula.information;

import org.apache.log4j.Logger;
import org.eclipse.jface.internal.text.InformationControlReplacer;
import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Instances of this class are watching a {@link Control} and consult a
 * {@link ISubjectInformationProvider} for information to be displayed in a
 * small popup window.
 * 
 * @author bkahlert
 * 
 * @param <CONTROL>
 * @param <INFORMATION>
 */
public class InformationControlManager<CONTROL extends Control, INFORMATION>
		extends AbstractHoverInformationControlManager {

	private static final Logger LOGGER = Logger
			.getLogger(InformationControlManager.class);

	private Listener listener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			if (event.keyCode != SWT.F2) {
				return;
			}
			// TODO only register one listener
			// TODO find out which popup to replace
			try {
				InformationControlManager.this.getInternalAccessor()
						.replaceInformationControl(false);
			} catch (Exception e) {
				LOGGER.error("Error while enhancing "
						+ InformationControl.class.getSimpleName());
			}
		}
	};

	private ISubjectInformationProvider<CONTROL, INFORMATION> subjectInformationProvider;

	/**
	 * Because {@link InformationControlManager} does not use the
	 * {@link InformationControlReplacer}'s provided
	 * {@link IInformationControlCreator} but the
	 * {@link InformationControlManager}'s one we can pass a fake
	 * {@link IInformationControlCreator}.
	 * 
	 * @author bkahlert
	 * 
	 * @param <INFORMATION>
	 */
	private static class FakeReusableInformationControlCreator<INFORMATION>
			extends InformationControlCreator<INFORMATION> {
		@Override
		protected InformationControl<INFORMATION> doCreateInformationControl(
				Shell parent) {
			return null;
		}
	}

	public InformationControlManager(
			InformationControlCreator<INFORMATION> creator,
			ISubjectInformationProvider<CONTROL, INFORMATION> subjectInformationProvider) {
		super(creator);
		StickyHoverManager<INFORMATION> replacer = new StickyHoverManager<INFORMATION>(
				new FakeReusableInformationControlCreator<INFORMATION>());
		this.getInternalAccessor().setInformationControlReplacer(replacer);
		this.subjectInformationProvider = subjectInformationProvider;
	}

	@Override
	public void install(Control subjectControl) {
		super.install(subjectControl);
		InformationControlReplacer replacer = this.getInternalAccessor()
				.getInformationControlReplacer();
		if (replacer != null) {
			replacer.install(subjectControl);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CONTROL getSubjectControl() {
		return (CONTROL) super.getSubjectControl();
	}

	@Override
	protected void computeInformation() {
		Point hoverArea = this.subjectInformationProvider
				.getHoverArea();
		if (hoverArea == null) {
			hoverArea = new Point(10, 10);
		}
		INFORMATION information = this.subjectInformationProvider
				.getInformation();

		Point mouseLocation = Display.getCurrent().getCursorLocation();
		Rectangle subjectArea = Geometry.toControl(this.getSubjectControl(),
				new Rectangle(mouseLocation.x - hoverArea.x / 2,
						mouseLocation.y - hoverArea.y / 2,
						hoverArea.x, hoverArea.y));

		this.setInformation(information, subjectArea);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setEnabled(boolean enabled) {
		boolean was = this.isEnabled();
		super.setEnabled(enabled);
		boolean is = this.isEnabled();
		if (was != is) {
			if (is) {
				if (this.subjectInformationProvider != null) {
					this.subjectInformationProvider.register(this
							.getSubjectControl());
				}
				Display.getCurrent().addFilter(SWT.KeyDown, this.listener);
			} else {
				Display.getCurrent().removeFilter(SWT.KeyDown, this.listener);
				if (this.subjectInformationProvider != null) {
					this.subjectInformationProvider.unregister(this
							.getSubjectControl());
				}
			}
		}
	}

	@Override
	public void dispose() {
		Display.getCurrent().removeFilter(SWT.KeyDown, this.listener);
		if (this.subjectInformationProvider != null) {
			this.subjectInformationProvider
					.unregister(this.getSubjectControl());
			this.subjectInformationProvider = null;
		}
		super.dispose();
	}

}