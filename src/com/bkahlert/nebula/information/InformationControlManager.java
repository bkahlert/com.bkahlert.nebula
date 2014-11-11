package com.bkahlert.nebula.information;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bkahlert.nebula.InformationManagerSourceProvider;

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

	private static Listener f2Filter = null;

	private static void activateF2Filter() {
		if (f2Filter != null) {
			return;
		}
		f2Filter = new Listener() {
			@SuppressWarnings("restriction")
			@Override
			public void handleEvent(Event event) {
				if (event.keyCode != SWT.F2) {
					return;
				}
				try {
					InformationControlManagerUtils.getCurrentManager()
							.getInternalAccessor()
							.replaceInformationControl(false);
				} catch (Exception e) {
					LOGGER.error("Error while enhancing "
							+ InformationControl.class.getSimpleName());
				}
			}
		};
		Display.getCurrent().addFilter(SWT.KeyDown, f2Filter);
	}

	private ISubjectInformationProvider<CONTROL, INFORMATION> subjectInformationProvider;
	private final Class<INFORMATION> informationClass;

	private final StickyHoverManager<INFORMATION> replacer;
	private Rectangle lastSubjectArea;

	@SuppressWarnings("restriction")
	public InformationControlManager(
			Class<INFORMATION> informationClass,
			InformationControlCreator<INFORMATION> creator,
			ISubjectInformationProvider<CONTROL, INFORMATION> subjectInformationProvider) {
		super(creator);
		this.replacer = new StickyHoverManager<INFORMATION>(creator);
		this.getInternalAccessor().setInformationControlReplacer(this.replacer);
		this.subjectInformationProvider = subjectInformationProvider;
		this.informationClass = informationClass;
	}

	public Class<INFORMATION> getInformationClass() {
		return this.informationClass;
	}

	@Override
	public void install(Control subjectControl) {
		super.install(subjectControl);
		this.replacer.install(subjectControl);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CONTROL getSubjectControl() {
		return (CONTROL) super.getSubjectControl();
	}

	@Override
	public void showInformation() {
		super.showInformation();
		System.err.println(this.getInformationControl());
	}

	/**
	 * Returns whether the standard or enriched version of the
	 * {@link InformationControl} is shown.
	 *
	 * @return
	 */
	public boolean isShowingEnriched() {
		@SuppressWarnings("restriction")
		InformationControl<?> enhancedControl = (InformationControl<?>) this.replacer
				.getCurrentInformationControl2();
		return enhancedControl != null && enhancedControl.isVisible();
	}

	/**
	 * In contrast to {@link #showInformation()} this method does not show what
	 * the {@link #subjectInformationProvider} returns but what you provide.
	 *
	 * @param information
	 */
	@SuppressWarnings("restriction")
	public void setInformation(INFORMATION information) {
		Rectangle subjectArea = new Rectangle(this.lastSubjectArea.x,
				this.lastSubjectArea.y, this.lastSubjectArea.width,
				this.lastSubjectArea.height);
		if (this.isShowingEnriched()) {
			@SuppressWarnings({ "unchecked" })
			InformationControl<CONTROL> informationControl = (InformationControl<CONTROL>) this.replacer
					.getCurrentInformationControl2();
			informationControl.setInput(information);
			if (!informationControl.hasContents()) {
				return;
			}

			if (informationControl != null) {
				Point sizeConstraints = this.computeSizeConstraints(
						this.getSubjectControl(), subjectArea,
						informationControl);
				Rectangle trim = informationControl.computeTrim();
				sizeConstraints.x += trim.width;
				sizeConstraints.y += trim.height;
				informationControl.setSizeConstraints(sizeConstraints.x,
						sizeConstraints.y);

				Point size = null;
				Point location = null;
				Rectangle bounds = this.restoreInformationControlBounds();

				if (bounds != null) {
					if (bounds.x > -1 && bounds.y > -1) {
						location = Geometry.getLocation(bounds);
					}

					if (bounds.width > -1 && bounds.height > -1) {
						size = Geometry.getSize(bounds);
					}
				}

				if (size == null) {
					size = informationControl.computeSizeHint();
				}

				// if (this.fEnforceAsMinimalSize) {
				// size = Geometry.max(size, sizeConstraints);
				// }
				// if (this.fEnforceAsMaximalSize) {
				// size = Geometry.min(size, sizeConstraints);
				// }

				if (location == null) {
					location = this.computeInformationControlLocation(
							subjectArea, size);
				}

				Rectangle controlBounds = Geometry.createRectangle(location,
						size);
				InformationControlManagerUtils.cropToClosestMonitor(this
						.getSubjectControl().getDisplay(), controlBounds);
				location = Geometry.getLocation(controlBounds);
				size = Geometry.getSize(controlBounds);
				informationControl.setLocation(location);
				informationControl.setSize(size.x, size.y);

				this.showInformationControl(subjectArea);
			}
			this.getInternalAccessor().replaceInformationControl(false);
		} else {
			this.setInformation(information, subjectArea);
		}
	}

	@Override
	protected void computeInformation() {
		InformationManagerSourceProvider.managerChanged(this);

		this.lastSubjectArea = this.calculateSubjectArea();
		INFORMATION information = this.subjectInformationProvider
				.getInformation();

		this.setInformation(information, this.lastSubjectArea);
	}

	/**
	 * Calculates the subject area based on the current cursor location.
	 *
	 * @return
	 */
	protected Rectangle calculateSubjectArea() {
		Point hoverArea = this.subjectInformationProvider.getHoverArea();
		if (hoverArea == null) {
			hoverArea = new Point(10, 10);
		}
		Point mouseLocation = Display.getCurrent().getCursorLocation();
		Rectangle bounds = Geometry.toControl(this.getSubjectControl(),
				new Rectangle(mouseLocation.x - hoverArea.x / 2,
						mouseLocation.y - hoverArea.y / 2, hoverArea.x,
						hoverArea.y));
		return bounds;
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
				activateF2Filter();
				// Display.getCurrent().addFilter(SWT.KeyDown, this.listener);
			} else {
				// Display.getCurrent().removeFilter(SWT.KeyDown,
				// this.listener);
				if (this.subjectInformationProvider != null) {
					this.subjectInformationProvider.unregister(this
							.getSubjectControl());
				}
			}
		}
	}

	@Override
	public void dispose() {
		// Display.getCurrent().removeFilter(SWT.KeyDown, this.listener);
		if (this.subjectInformationProvider != null) {
			this.subjectInformationProvider
					.unregister(this.getSubjectControl());
			this.subjectInformationProvider = null;
		}
		super.dispose();
	}

}