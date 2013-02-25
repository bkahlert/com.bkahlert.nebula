package com.bkahlert.devel.nebula.widgets.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;

/**
 * This is a wrapped CKEditor (ckeditor.com).
 * <p>
 * <b>Important developer warning</b>: Do not try to wrap a WYSIWYG editor based
 * on iframes like TinyMCE. Some internal browsers (verified with Webkit) do not
 * handle cut, copy and paste actions when the iframe is in focus. CKEditor can
 * operate in both modes - iframes and divs (and p tags).
 * 
 * @author bkahlert
 * 
 */
public class Editor extends BrowserComposite {

	private static final Logger LOGGER = Logger.getLogger(Editor.class);

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

	private List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();
	private String oldHtml = "";
	private Timer delayChangeTimer = null;

	public Editor(Composite parent, int style) {
		this(parent, style, 0);
	}

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param delayChangeEventUpTo
	 *            is the delay that must have been passed in order to fire a
	 *            change event. If 0 no delay will be applied. The minimal delay
	 *            is defined by the CKEditor's config.js.
	 */
	public Editor(Composite parent, int style, final long delayChangeEventUpTo) {
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

		this.getBrowser().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				System.err.println(e.keyCode);
				if ((e.stateMask & SWT.CTRL) != 0
						|| (e.stateMask & SWT.COMMAND) != 0) {
					if (e.keyCode == 97) {
						// a - select all
						selectAll();
					}

					/*
					 * The CKEditor plugin "onchange" does not notify on cut and
					 * paste operations. Therefore we need to handle them here.
					 */
					if (e.keyCode == 120) {
						// x - cut
						// wait for the ui thread to apply the operation
						ExecutorUtil.asyncExec(new Runnable() {
							public void run() {
								modified(getSource(), delayChangeEventUpTo);
							}
						});
					}
					if (e.keyCode == 99) {
						// c - copy
						// do nothing
					}
					if (e.keyCode == 118) {
						// v - paste
						// wait for the ui thread to apply the operation
						ExecutorUtil.asyncExec(new Runnable() {
							public void run() {
								modified(getSource(), delayChangeEventUpTo);
							}
						});
					}
				}

			}
		});

		this.getBrowser().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				modified(getSource(), 0);
			}
		});

		new ModifyListenerBrowserFunction(this.getBrowser()) {
			@Override
			public void call(String newHtml) {
				modified(newHtml, delayChangeEventUpTo);
			}
		};
	}

	protected void modified(String html, long delayChangeEventUpTo) {
		final String newHtml = (html == null || html.trim().isEmpty()) ? ""
				: html;
		if (oldHtml.equals(newHtml))
			return;

		final Runnable fireRunnable = new Runnable() {
			@Override
			public void run() {
				Event event = new Event();
				event.display = Display.getCurrent();
				event.time = (int) (new Date().getTime() & 0xFFFFFFFFL);
				event.widget = (Widget) Editor.this;
				event.text = newHtml;
				event.data = newHtml;
				ModifyEvent modifyEvent = new ModifyEvent(event);
				for (ModifyListener modifyListener : modifyListeners) {
					modifyListener.modifyText(modifyEvent);
				}
			}
		};

		oldHtml = html;

		if (delayChangeTimer != null)
			delayChangeTimer.cancel();
		if (delayChangeEventUpTo > 0) {
			delayChangeTimer = new Timer();
			delayChangeTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					fireRunnable.run();
				}
			}, delayChangeEventUpTo);
		} else {
			delayChangeTimer = null;
			fireRunnable.run();
		}
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

	public void selectAll() {
		String js = "com.bkahlert.devel.nebula.editor.selectAll();";
		this.enqueueJs(js);
	}

	public void setSource(String html) {
		String js = "com.bkahlert.devel.nebula.editor.setSource("
				+ TimelineJsonGenerator.enquote(html) + ");";
		this.enqueueJs(js);
	}

	public String getSource() {
		if (!this.isLoadingCompleted())
			return null;
		String html = (String) this.getBrowser().evaluate(
				"return com.bkahlert.devel.nebula.editor.getSource();");
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

	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		String js = "com.bkahlert.devel.nebula.editor.setEnabled("
				+ (isEnabled ? "true" : "false") + ");";
		this.enqueueJs(js);
	}

	public void addModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.add(modifyListener);
	}

	public void removeModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.remove(modifyListener);
	}

}
