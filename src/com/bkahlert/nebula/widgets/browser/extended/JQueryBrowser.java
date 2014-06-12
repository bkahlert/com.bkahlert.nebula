package com.bkahlert.nebula.widgets.browser.extended;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.widgets.browser.extended.ISelector.IdSelector;
import com.bkahlert.nebula.widgets.browser.extended.ISelector.NameSelector;
import com.bkahlert.nebula.widgets.browser.extended.extensions.IBrowserExtension;
import com.bkahlert.nebula.widgets.browser.extended.extensions.jquery.JQueryBrowserExtension;
import com.bkahlert.nebula.widgets.browser.extended.html.Element;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;

public class JQueryBrowser extends ExtendedBrowser implements IJQueryBrowser {
	private static final Logger LOGGER = Logger.getLogger(JQueryBrowser.class);

	private Point disposedScrollPositon = null;

	public JQueryBrowser(Composite parent, int style) {
		this(parent, style, new IBrowserExtension[] {});
	}

	@SuppressWarnings("serial")
	public JQueryBrowser(Composite parent, int style,
			final IBrowserExtension[] extensions) {
		super(parent, style, new ArrayList<IBrowserExtension>() {
			{
				this.add(new JQueryBrowserExtension());
				if (extensions != null) {
					this.addAll(Arrays.asList(extensions));
				}
			}
		}.toArray(new IBrowserExtension[0]));
		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				try {
					JQueryBrowser.this.disposedScrollPositon = JQueryBrowser.this
							.getScrollPosition().get();
				} catch (Exception e1) {
					Throwable ex = e1;
					while (ex.getCause() != null) {
						ex = ex.getCause();
					}
					if (ex instanceof SWTException) {
						// very propably widget disposed
					} else {
						LOGGER.error("Error saving state of "
								+ JQueryBrowser.this, e1);
					}
				}
			}
		});
	}

	private String getFocusStmt(ISelector selector) {
		return "$('" + selector + "').focus()";
	}

	private String getBlurStmt(ISelector selector) {
		return "$('" + selector + "').blur()";
	}

	private String getKeyUpStmt(ISelector selector) {
		return "$('" + selector + "').keyup()";
	}

	private String getKeyDownStmt(ISelector selector) {
		return "$('" + selector + "').keydown()";
	}

	private String getKeyPressStmt(ISelector selector) {
		return "$('" + selector + "').keypress()";
	}

	private String getSubmitStmt(ISelector selector) {
		return "$('" + selector + "').closest('form').submit();";
	}

	private String getValStmt(ISelector selector, String value) {
		return "$('" + selector + "').val('" + value + "');";
	}

	/**
	 * function simulateKeyPress($elements, eventType) { var keyboardEvent =
	 * document.createEvent("KeyboardEvent"); var initMethod = typeof
	 * keyboardEvent.initKeyboardEvent !== 'undefined' ? "initKeyboardEvent" :
	 * "initKeyEvent"; keyboardEvent[initMethod]( eventType, // event type :
	 * keydown, keyup, keypress true, // bubbles true, // cancelable window, //
	 * viewArg: should be window false, // ctrlKeyArg false, // altKeyArg false,
	 * // shiftKeyArg false, // metaKeyArg 16, // keyCodeArg : unsigned long the
	 * virtual key code, else 0 0 // charCodeArgs : unsigned long the Unicode
	 * character associated with the depressed key, else 0 );
	 * $elements.each(function() { this.dispatchEvent(keyboardEvent); }); }
	 * 
	 * @see http 
	 *      ://stackoverflow.com/questions/596481/simulate-javascript-key-events
	 */
	private String getSimulateKeyPress() {
		return "function simulateKeyPress(e,t){var n=document.createEvent('KeyboardEvent');var r=typeof n.initKeyboardEvent!=='undefined'?'initKeyboardEvent':'initKeyEvent';n[r](t,true,true,window,false,false,false,false,16,0);e.each(function(){this.dispatchEvent(n)})}";
	}

	private String getForceKeyPressStmt(ISelector selector) {
		return this.getSimulateKeyPress() + "simulateKeyPress($('" + selector
				+ "'), 'keydown');" + "simulateKeyPress($('" + selector
				+ "'), 'keypress');" + "simulateKeyPress($('" + selector
				+ "'), 'keyup');";
	}

	@Override
	public Future<Boolean> containsElements(ISelector selector) {
		return this.run("return $('" + selector.toString() + "').length > 0;",
				IConverter.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Point> getScrollPosition() {
		if (this.disposedScrollPositon != null) {
			return new CompletedFuture<Point>(this.disposedScrollPositon, null);
		}
		return JQueryBrowser.this
				.run("return [jQuery(document).scrollLeft(),jQuery(document).scrollTop()];",
						IConverter.CONVERTER_POINT);
	}

	@Override
	public Future<Point> getScrollPosition(final ISelector selector) {
		String jQuery = "jQuery('" + selector + "')";
		if (selector instanceof IdSelector) {
			// preferred if id contains special characters
			jQuery = "jQuery(document.getElementById(\""
					+ ((IdSelector) selector).getId() + "\"))";
		}
		if (selector instanceof NameSelector) {
			// preferred if name contains special characters
			jQuery = "jQuery(document.getElementsByName(\""
					+ ((NameSelector) selector).getName() + "\")[0])";
		}
		return JQueryBrowser.this
				.run("var offset = "
						+ jQuery
						+ ".offset(); return offset ? [offset.left, offset.top] : null;",
						IConverter.CONVERTER_POINT);
	}

	@Override
	public Future<Point> getRelativePosition(final ISelector selector) {
		return JQueryBrowser.this
				.run("var offset = jQuery('"
						+ selector
						+ "').offset();return [offset.left-jQuery(document).scrollLeft(),offset.top-jQuery(document).scrollTop()];",
						IConverter.CONVERTER_POINT);
	}

	@Override
	public Future<Boolean> scrollTo(final int x, final int y) {
		String script = String
				.format("if(jQuery(document).scrollLeft()!=%d||jQuery(document).scrollTop()!=%d){jQuery('html, body').animate({ scrollLeft: %d, scrollTop: %d }, 0);return true;}else{return false;}",
						x, y, x, y);
		return JQueryBrowser.this.run(script, IConverter.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Boolean> scrollTo(Point pos) {
		return this.scrollTo(pos.x, pos.y);
	}

	@Override
	public Future<Boolean> scrollTo(final ISelector selector) {
		return ExecUtils.nonUIAsyncExec(JQueryBrowser.class, "Scroll To",
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						Point pos = JQueryBrowser.this.getScrollPosition(
								selector).get();
						return JQueryBrowser.this.scrollTo(pos).get();
					}
				});
	}

	@Override
	public Future<Object> focus(ISelector selector) {
		return this.run(this.getFocusStmt(selector));
	}

	@Override
	public Future<IElement> getFocusedElement() {
		return ExecUtils.nonUIAsyncExec(JQueryBrowser.class,
				"Get Focused Element", new Callable<IElement>() {
					@Override
					public IElement call() throws Exception {
						try {
							String html = JQueryBrowser.this
									.run("return jQuery(document.activeElement).clone().wrap(\"<p>\").parent().html();",
											IConverter.CONVERTER_STRING).get();
							return new Element(html);
						} catch (Exception e) {
							LOGGER.error("Error getting scroll position", e);
						}
						return null;
					}
				});
	}

	@Override
	public Future<Object> blur(ISelector selector) {
		return this.run(this.getBlurStmt(selector));
	}

	@Override
	public Future<Object> keyUp(ISelector selector) {
		return this.run(this.getKeyUpStmt(selector));
	}

	@Override
	public Future<Object> keyDown(ISelector selector) {
		return this.run(this.getKeyDownStmt(selector));
	}

	@Override
	public Future<Object> keyPress(ISelector selector) {
		return this.run(this.getKeyPressStmt(selector));
	}

	@Override
	public Future<Object> forceKeyPress(ISelector selector) {
		return this.run(this.getForceKeyPressStmt(selector));
	}

	@Override
	public Future<Object> simulateTyping(final ISelector selector,
			final String text) {
		// return ExecutorUtil.nonUIAsyncExec(new Callable<Object>() {
		// @Override
		// public Object call() throws Exception {
		// Rectangle browserBounds = ShellUtils
		// .getInnerArea(BootstrapBrowser.this);
		// Point elementRelativePos = BootstrapBrowser.this
		// .getRelativePosition(selector).get();
		// Point whereToClickToFocus = new Point(browserBounds.x
		// + elementRelativePos.x + 3, browserBounds.y
		// + elementRelativePos.y + 3);
		// Robot robot = ShellUtils.getRobot();
		// robot.mouseMove(whereToClickToFocus.x, whereToClickToFocus.y);
		// robot.mousePress(InputEvent.BUTTON1_MASK);
		// robot.mouseRelease(InputEvent.BUTTON1_MASK);
		// for (int i = 0; i < text.length(); i++) {
		// robot.keyPress(text.codePointAt(i));
		// }
		// return null;
		// }
		// });
		return this.run(this.getFocusStmt(selector)
				+ this.getValStmt(selector, text)
				+ this.getForceKeyPressStmt(selector)
				+ this.getBlurStmt(selector));
	}

	@Override
	public Future<Object> val(ISelector selector, String value) {
		return this.run(this.getValStmt(selector, value));
	}

	@Override
	public Future<Object> submit(ISelector selector) {
		return this.run(this.getSubmitStmt(selector));
	}

}
