package com.bkahlert.devel.nebula.utils.information;

import org.eclipse.jface.internal.text.InformationControlReplacer;
import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

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
public class TypedInformationControlManager<CONTROL extends Control, INFORMATION>
		extends AbstractHoverInformationControlManager {

	private ISubjectInformationProvider<CONTROL, INFORMATION> subjectInformationProvider;

	public TypedInformationControlManager(
			TypedReusableInformationControlCreator<INFORMATION> creator,
			ISubjectInformationProvider<CONTROL, INFORMATION> subjectInformationProvider) {
		super(creator);
		this.getInternalAccessor().setInformationControlReplacer(
				new StickyHoverManager<INFORMATION>(creator));
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
		Point informationSize = this.subjectInformationProvider
				.getInformationSize();
		if (informationSize == null) {
			informationSize = new Point(10, 10);
		}
		INFORMATION information = this.subjectInformationProvider
				.getInformation();

		Point mouseLocation = Display.getCurrent().getCursorLocation();
		Rectangle subjectArea = Geometry.toControl(this.getSubjectControl(),
				new Rectangle(mouseLocation.x - informationSize.x / 2,
						mouseLocation.y - informationSize.y / 2,
						informationSize.x, informationSize.y));

		this.setInformation(information, subjectArea);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setEnabled(boolean enabled) {
		boolean was = this.isEnabled();
		super.setEnabled(enabled);
		boolean is = this.isEnabled();

		if (was != is && this.subjectInformationProvider != null) {
			if (is) {
				this.subjectInformationProvider.register(this
						.getSubjectControl());
			} else {
				this.subjectInformationProvider.unregister(this
						.getSubjectControl());
			}
		}
	}

	@Override
	public void dispose() {
		if (this.subjectInformationProvider != null) {
			this.subjectInformationProvider
					.unregister(this.getSubjectControl());
			this.subjectInformationProvider = null;
		}
		super.dispose();
	}

}