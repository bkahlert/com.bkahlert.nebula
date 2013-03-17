package com.bkahlert.devel.nebula.utils.information;

import org.eclipse.jface.internal.text.InformationControlReplacer;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Instances of this class serve as a {@link InformationControlReplacer} that
 * hide when the mouse leaves the popup control.
 * <p>
 * Originally copied from
 * {@link org.eclipse.jface.internal.text.StickyHoverManager}.
 * 
 * @author bkahlert
 * 
 * @param <INFORMATION>
 */
public class StickyHoverManager<INFORMATION> extends InformationControlReplacer {

	/**
	 * Internal information control closer. Listens to several events issued by
	 * its subject control and closes the information control when necessary.
	 */
	class Closer implements IInformationControlCloser, ControlListener,
			MouseListener, KeyListener, FocusListener, Listener {
		// TODO: Catch 'Esc' key in fInformationControlToClose: Don't dispose,
		// just hideInformationControl().
		// This would allow to reuse the information control also when the user
		// explicitly closes it.

		// TODO: if subject control is a Scrollable, should add selection
		// listeners to both scroll bars
		// (and remove the ViewPortListener, which only listens to vertical
		// scrolling)

		/** The subject control. */
		private Control fSubjectControl;
		/** Indicates whether this closer is active. */
		private boolean fIsActive = false;
		/** The display. */
		private Display fDisplay;

		/*
		 * @see IInformationControlCloser#setSubjectControl(Control)
		 */
		@Override
		public void setSubjectControl(Control control) {
			this.fSubjectControl = control;
		}

		/*
		 * @see
		 * IInformationControlCloser#setInformationControl(IInformationControl)
		 */
		@Override
		public void setInformationControl(IInformationControl control) {
			// NOTE: we use getCurrentInformationControl2() from the outer class
		}

		/*
		 * @see IInformationControlCloser#start(Rectangle)
		 */
		@Override
		public void start(Rectangle informationArea) {

			if (this.fIsActive) {
				return;
			}
			this.fIsActive = true;

			if (this.fSubjectControl != null
					&& !this.fSubjectControl.isDisposed()) {
				this.fSubjectControl.addControlListener(this);
				this.fSubjectControl.addMouseListener(this);
				this.fSubjectControl.addKeyListener(this);
			}

			IInformationControl fInformationControlToClose = StickyHoverManager.this
					.getCurrentInformationControl2();
			if (fInformationControlToClose != null) {
				fInformationControlToClose.addFocusListener(this);
			}

			this.fDisplay = this.fSubjectControl.getDisplay();
			if (!this.fDisplay.isDisposed()) {
				this.fDisplay.addFilter(SWT.MouseMove, this);
				this.fDisplay.addFilter(SWT.FocusOut, this);
			}
		}

		/*
		 * @see IInformationControlCloser#stop()
		 */
		@Override
		public void stop() {

			if (!this.fIsActive) {
				return;
			}
			this.fIsActive = false;

			if (this.fSubjectControl != null
					&& !this.fSubjectControl.isDisposed()) {
				this.fSubjectControl.removeControlListener(this);
				this.fSubjectControl.removeMouseListener(this);
				this.fSubjectControl.removeKeyListener(this);
			}

			IInformationControl fInformationControlToClose = StickyHoverManager.this
					.getCurrentInformationControl2();
			if (fInformationControlToClose != null) {
				fInformationControlToClose.removeFocusListener(this);
			}

			if (this.fDisplay != null && !this.fDisplay.isDisposed()) {
				this.fDisplay.removeFilter(SWT.MouseMove, this);
				this.fDisplay.removeFilter(SWT.FocusOut, this);
			}

			this.fDisplay = null;
		}

		/*
		 * @see ControlListener#controlResized(ControlEvent)
		 */
		@Override
		public void controlResized(ControlEvent e) {
			StickyHoverManager.this.hideInformationControl();
		}

		/*
		 * @see ControlListener#controlMoved(ControlEvent)
		 */
		@Override
		public void controlMoved(ControlEvent e) {
			StickyHoverManager.this.hideInformationControl();
		}

		/*
		 * @see MouseListener#mouseDown(MouseEvent)
		 */
		@Override
		public void mouseDown(MouseEvent e) {
			StickyHoverManager.this.hideInformationControl();
		}

		/*
		 * @see MouseListener#mouseUp(MouseEvent)
		 */
		@Override
		public void mouseUp(MouseEvent e) {
		}

		/*
		 * @see MouseListener#mouseDoubleClick(MouseEvent)
		 */
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			StickyHoverManager.this.hideInformationControl();
		}

		/*
		 * @see KeyListener#keyPressed(KeyEvent)
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			StickyHoverManager.this.hideInformationControl();
		}

		/*
		 * @see KeyListener#keyReleased(KeyEvent)
		 */
		@Override
		public void keyReleased(KeyEvent e) {
		}

		/*
		 * @see
		 * org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.
		 * events.FocusEvent)
		 */
		@Override
		public void focusGained(FocusEvent e) {
		}

		/*
		 * @see
		 * org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events
		 * .FocusEvent)
		 */
		@Override
		public void focusLost(FocusEvent e) {
			if (DEBUG) {
				System.out
						.println("StickyHoverManager.Closer.focusLost(): " + e); //$NON-NLS-1$
			}
			Display d = this.fSubjectControl.getDisplay();
			d.asyncExec(new Runnable() {
				// Without the asyncExec, mouse clicks to the workbench window
				// are swallowed.
				@Override
				public void run() {
					StickyHoverManager.this.hideInformationControl();
				}
			});
		}

		/*
		 * @see
		 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets
		 * .Event)
		 */
		@Override
		public void handleEvent(Event event) {
			if (event.type == SWT.MouseMove) {
				if (!(event.widget instanceof Control)
						|| event.widget.isDisposed()) {
					return;
				}

				IInformationControl infoControl = StickyHoverManager.this
						.getCurrentInformationControl2();
				if (infoControl != null && !infoControl.isFocusControl()
						&& infoControl instanceof IInformationControlExtension3) {
					//					if (DEBUG) System.out.println("StickyHoverManager.Closer.handleEvent(): activeShell= " + fDisplay.getActiveShell()); //$NON-NLS-1$
					IInformationControlExtension3 iControl3 = (IInformationControlExtension3) infoControl;
					Rectangle controlBounds = iControl3.getBounds();
					if (controlBounds != null) {
						Point mouseLoc = event.display.map(
								(Control) event.widget, null, event.x, event.y);
						int margin = StickyHoverManager.this.getKeepUpMargin();
						Geometry.expand(controlBounds, margin, margin, margin,
								margin);
						if (!controlBounds.contains(mouseLoc)) {
							StickyHoverManager.this.hideInformationControl();
						}
					}

				} else {
					/*
					 * TODO: need better understanding of why/if this is needed.
					 * Looks like the same panic code we have in
					 * org.eclipse.jface
					 * .text.AbstractHoverInformationControlManager
					 * .Closer.handleMouseMove(Event)
					 */
					if (this.fDisplay != null && !this.fDisplay.isDisposed()) {
						this.fDisplay.removeFilter(SWT.MouseMove, this);
					}
				}

			} else if (event.type == SWT.FocusOut) {
				if (DEBUG) {
					System.out
							.println("StickyHoverManager.Closer.handleEvent(): focusOut: " + event); //$NON-NLS-1$
				}
				IInformationControl iControl = StickyHoverManager.this
						.getCurrentInformationControl2();
				if (iControl != null && !iControl.isFocusControl()) {
					StickyHoverManager.this.hideInformationControl();
				}
			}
		}
	}

	/**
	 * Creates a new sticky hover manager.
	 * 
	 * @param textViewer
	 *            the text viewer
	 */
	public StickyHoverManager(
			InformationControlCreator<INFORMATION> creator) {
		super(creator);
		this.setCloser(new Closer());
	}

}
