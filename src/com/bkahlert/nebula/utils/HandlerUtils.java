package com.bkahlert.nebula.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.swt.IFocusService;

import com.bkahlert.nebula.utils.ClipboardListener.IClipboardTransferChangeListener;

/**
 * Utility functions for the <a
 * href="http://wiki.eclipse.org/Platform_Command_Framework">Eclipse Platform
 * Command Framework</a>.
 * 
 * @author bkahlert
 * 
 */
public class HandlerUtils {

	protected static class CustomPasteHandlerManager {

		private static final Logger LOGGER = Logger
				.getLogger(CustomPasteHandlerManager.class);

		private final Clipboard clipboard = new Clipboard(Display.getCurrent());
		private final ClipboardListener clipboardListener = new ClipboardListener(
				200l);
		private final IClipboardTransferChangeListener clipboardTransferChangeListener = new IClipboardTransferChangeListener() {
			@Override
			public void transferChanged() {
				CustomPasteHandlerManager.this.adaptCustomPasteHandler();
			}
		};

		private final IFocusService FOCUS_SERVICE = (IFocusService) PlatformUI
				.getWorkbench().getService(IFocusService.class);

		private class FocusListener implements
				org.eclipse.swt.events.FocusListener {

			private final Control control;

			public FocusListener(Control control) {
				this.control = control;
			}

			@Override
			public void focusGained(FocusEvent e) {
				LOGGER.debug("Focus gained: " + this.control);
				CustomPasteHandlerManager.this.focusControl = this.control;
				CustomPasteHandlerManager.this.clipboardListener
						.addClipboardTransferChangeListener(CustomPasteHandlerManager.this.clipboardTransferChangeListener);
				CustomPasteHandlerManager.this.adaptCustomPasteHandler();
			}

			@Override
			public void focusLost(FocusEvent e) {
				LOGGER.debug("Focus lost: " + this.control);
				CustomPasteHandlerManager.this.focusControl = null;
				CustomPasteHandlerManager.this.clipboardListener
						.removeClipboardTransferChangeListener(CustomPasteHandlerManager.this.clipboardTransferChangeListener);
				CustomPasteHandlerManager.this.adaptCustomPasteHandler();
			}
		}

		private final Map<Control, FocusListener> focusListeners = new HashMap<Control, CustomPasteHandlerManager.FocusListener>();
		private final Map<Control, String> focusControlIds = new HashMap<Control, String>();
		private final Map<Control, List<String>> customIfMap = new HashMap<Control, List<String>>();
		private Control focusControl = null;

		public CustomPasteHandlerManager() {
			this.clipboardListener.start();
		}

		public void activateCustomPasteHandlerConsideration(
				final Control control, String focusControlId, String[] mimeTypes) {
			this.deactivateCustomPasteHandlerConsideration(control);

			this.focusControlIds.put(control, focusControlId);
			this.customIfMap.put(control, Arrays.asList(mimeTypes));
			this.focusListeners.put(control, new FocusListener(control));

			control.addFocusListener(this.focusListeners.get(control));
			control.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					CustomPasteHandlerManager.this
							.deactivateCustomPasteHandlerConsideration(control);
				}
			});

			LOGGER.debug("Registered mime type \"" + mimeTypes
					+ "\" for custom paste handler: " + control);
		}

		public void deactivateCustomPasteHandlerConsideration(Control control) {
			if (this.focusControlIds.containsKey(control)) {
				this.focusControlIds.remove(control);
			}
			if (this.customIfMap.containsKey(control)) {
				this.customIfMap.remove(control);
			}
			if (this.focusListeners.containsKey(control)) {
				control.removeFocusListener(this.focusListeners.get(control));
				this.focusListeners.remove(control);
			}

			LOGGER.debug("Unregistered custom paste handler: " + control);
		}

		private void adaptCustomPasteHandler() {
			for (Control control : this.customIfMap.keySet()) {
				if (control == this.focusControl) {
					continue;
				}
				this.FOCUS_SERVICE.removeFocusTracker(control);
				LOGGER.info("Removed focus tracker: " + control);
			}
			if (this.focusControl != null) {
				List<Transfer> customTransfers = new ArrayList<Transfer>();
				for (String mimeType : this.customIfMap.get(this.focusControl)) {
					if (ClipboardListener.mimeTypeMapping.containsKey(mimeType)) {
						Transfer customTransfer = ClipboardListener.mimeTypeMapping
								.get(mimeType);
						if (!customTransfers.contains(customTransfer)) {
							customTransfers.add(customTransfer);
						}
					} else {
						LOGGER.warn("A custom paste handler is intended to be activated for "
								+ Control.class.getSimpleName()
								+ ". Since the registered mime type \""
								+ mimeType
								+ "\" is unknown, the default paste handler is used. Please double check and if correct add it to "
								+ ClipboardListener.class.getSimpleName() + ".");
					}
				}

				boolean useCustomPasteHandler = false;
				outer: for (TransferData transferData : this.clipboard
						.getAvailableTypes()) {
					for (Transfer customTransfer : customTransfers) {
						if (customTransfer.isSupportedType(transferData)) {
							useCustomPasteHandler = true;
							break outer;
						}
					}
				}

				if (useCustomPasteHandler) {
					CustomPasteHandlerManager.this.FOCUS_SERVICE
							.addFocusTracker(this.focusControl,
									this.focusControlIds.get(this.focusControl));
					LOGGER.info("Added focus tracker: " + this.focusControl);
				} else {
					this.FOCUS_SERVICE.removeFocusTracker(this.focusControl);
				}

				/*
				 * FIXME terrible work-around. This is really messy but the
				 * expression context that is responsible to effectively
				 * (de)activate the custom paste handler does not get evaluated
				 * until a new focus/blur event occurs. Therefore we need to
				 * trigger one on our own
				 */
				try {
					Method m = this.FOCUS_SERVICE.getClass().getDeclaredMethod(
							"focusIn", Widget.class);
					m.setAccessible(true);
					m.invoke(this.FOCUS_SERVICE, new Object[] { null });
					m.invoke(this.FOCUS_SERVICE,
							new Object[] { this.focusControl });
				} catch (Exception e) {
					LOGGER.warn(
							"Could not simulate a blur/focus event. The custom paste handler won't work as expected.",
							e);
				}
			}
		}
	}

	private static CustomPasteHandlerManager customPasteHandlerManagerInstance = new CustomPasteHandlerManager();

	/**
	 * Activates the possibility that a custom paste handler get used for the
	 * given control and clipboard content type (a.k.a flavor).
	 * <p>
	 * For this to work, a handler needs to be defined in
	 * <code>plugin.xml</code> that uses command id
	 * <code>org.eclipse.ui.edit.paste</code> and activates if the
	 * <code>activeFocusControlId</code> has the given name. See <a href=
	 * "http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fui%2Fswt%2FIFocusService.html"
	 * >Interface IFocusService</a> for more information.
	 * 
	 * @param control
	 * @param mimeTypes
	 */
	public static void activateCustomPasteHandlerConsideration(Control control,
			String focusControlId, String... mimeTypes) {
		customPasteHandlerManagerInstance
				.activateCustomPasteHandlerConsideration(control,
						focusControlId, mimeTypes);
	}

	public static void deactivateCustomPasteHandlerConsideration(Control control) {
		customPasteHandlerManagerInstance
				.deactivateCustomPasteHandlerConsideration(control);
	}

}
