package com.bkahlert.devel.nebula.widgets.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;

/**
 * This is a wrapped TinyMCE editor.
 * 
 * @author bkahlert
 * 
 */
public class Editor extends BrowserComposite {

	private static final Logger LOGGER = Logger.getLogger(Editor.class);

	private List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();

	private static abstract class ModifyListenerBrowserFunction extends
			BrowserFunction {

		public ModifyListenerBrowserFunction(Browser browser) {
			super(browser, "modified");
		}

		@Override
		public Object function(Object[] arguments) {
			if (arguments.length >= 1) {
				if (arguments[0] instanceof String) {
					String newHtml = (String) arguments[0];
					call(newHtml);
				}
			}
			return super.function(arguments);
		}

		public abstract void call(String newHtml);
	}

	private static abstract class CutCopyPasteBrowserFunction extends
			BrowserFunction {
		public static enum Action {
			CUT, COPY, PASTE
		}

		public CutCopyPasteBrowserFunction(Browser browser, Action action) {
			super(browser, action.toString().toLowerCase());
		}

		@Override
		public Object function(Object[] arguments) {
			action();
			return null;
		}

		public abstract void action();
	}

	public Editor(Composite parent, int style) {
		super(parent, style);

		/*
		 * Deactivate browser's native context/popup menu. Doing so allows the
		 * definition of menus in an inheriting composite via setMenu.
		 */
		this.getBrowser().addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
			}
		});

		new ModifyListenerBrowserFunction(this.getBrowser()) {
			@Override
			public void call(String newHtml) {
				Event event = new Event();
				event.display = Display.getCurrent();
				event.time = (int) (new Date().getTime() & 0xFFFFFFFFL);
				event.widget = (Widget) Editor.this;
				event.text = newHtml;
				ModifyEvent modifyEvent = new ModifyEvent(event);
				for (ModifyListener modifyListener : modifyListeners) {
					modifyListener.modifyText(modifyEvent);
				}
			}
		};

		/*
		 * BUGGY BUGGY BUGGY - Eclipse's internal Webkit browser (possibly
		 * others affected as well) don't execute cut, copy and paste events
		 * correctly when the an iframe is focused within an internal browser.
		 * That's why the inner html page is watching for cut, copy and paste
		 * shortcuts and calling the global function without any arguments.
		 */
		new CutCopyPasteBrowserFunction(this.getBrowser(),
				CutCopyPasteBrowserFunction.Action.CUT) {
			@Override
			public void action() {
				// TODO Auto-generated method stub

			}
		};
		new CutCopyPasteBrowserFunction(this.getBrowser(),
				CutCopyPasteBrowserFunction.Action.COPY) {
			@Override
			public void action() {
				// TODO Auto-generated method stub

			}
		};
		new CutCopyPasteBrowserFunction(this.getBrowser(),
				CutCopyPasteBrowserFunction.Action.PASTE) {
			@Override
			public void action() {
				Clipboard clipboard = new Clipboard(Display.getCurrent());
				System.err.println(new ArrayList<String>(Arrays
						.asList(clipboard.getAvailableTypeNames())));
			}
		};
	}

	@Override
	public String getStartUrl() {
		try {
			String editorUrlString = getFileUrl(Editor.class, "html/index.html");
			return editorUrlString + "?internal=true";
		} catch (IOException e) {
			LOGGER.error("Could not open editor html", e);
		}
		return null;
	}

	public Boolean isDirty() {
		Boolean isDirty = (Boolean) this.getBrowser().evaluate(
				"return com.bkahlert.devel.nebula.editor.isDirty();");
		if (isDirty != null)
			return isDirty;
		else
			return null;
	}

	public void setSource(String html) {
		String js = "com.bkahlert.devel.nebula.editor.setSource("
				+ TimelineJsonGenerator.enquote(html) + ");";
		this.enqueueJs(js);
	}

	public void insertSource(String html) {
		String js = "com.bkahlert.devel.nebula.editor.insertSource("
				+ TimelineJsonGenerator.enquote(html) + ");";
		this.enqueueJs(js);
	}

	public String getSource() {
		String html = (String) this.getBrowser().evaluate(
				"return com.bkahlert.devel.nebula.editor.getSource();");
		if (html != null)
			return StringEscapeUtils.unescapeHtml(html);
		else
			return null;
	}

	public String getSelectedSource() {
		String html = (String) this.getBrowser().evaluate(
				"return com.bkahlert.devel.nebula.editor.getSelectedSource();");
		if (html != null)
			return StringEscapeUtils.unescapeHtml(html);
		else
			return null;
	}

	public void showSource() {
		this.enqueueJs("com.bkahlert.devel.nebula.editor.showSource();");
	}

	public void hideSource() {
		this.enqueueJs("com.bkahlert.devel.nebula.editor.hideSource();");
	}

	public void addModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.add(modifyListener);
	}

	public void removeModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.remove(modifyListener);
	}

}
