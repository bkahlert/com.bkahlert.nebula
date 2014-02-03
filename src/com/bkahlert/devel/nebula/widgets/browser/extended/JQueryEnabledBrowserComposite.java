package com.bkahlert.devel.nebula.widgets.browser.extended;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.utils.IConverter;
import com.bkahlert.devel.nebula.widgets.browser.IJavaScript;
import com.bkahlert.devel.nebula.widgets.browser.JavaScript;
import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector.IdSelector;
import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector.NameSelector;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.IBrowserCompositeExtension;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.JQueryExtension;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.JQueryScrollToExtension;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.Element;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.IElement;

public class JQueryEnabledBrowserComposite extends ExtendedBrowserComposite
		implements IJQueryEnabledBrowserComposite {
	private static final Logger LOGGER = Logger
			.getLogger(JQueryEnabledBrowserComposite.class);

	private final List<IFocusListener> focusListeners = new ArrayList<IFocusListener>();

	public JQueryEnabledBrowserComposite(Composite parent, int style) {
		this(parent, style, new IBrowserCompositeExtension[] {});
	}

	@SuppressWarnings("serial")
	public JQueryEnabledBrowserComposite(Composite parent, int style,
			final IBrowserCompositeExtension[] extensions) {
		super(parent, style, new ArrayList<IBrowserCompositeExtension>() {
			{
				this.add(new JQueryExtension());
				this.add(new JQueryScrollToExtension());
				if (extensions != null) {
					this.addAll(Arrays.asList(extensions));
				}
			}
		}.toArray(new IBrowserCompositeExtension[0]));

		new BrowserFunction(this.getBrowser(), "__focus") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					final IElement element = new Element((String) arguments[0]);
					JQueryEnabledBrowserComposite.this
							.notifyFocusChanged(element);
				}
				return null;
			}
		};
	}

	@Override
	public void addFocusListener(IFocusListener focusListener) {
		this.focusListeners.add(focusListener);
	}

	@Override
	public void removeFocusListener(IFocusListener focusListener) {
		this.focusListeners.remove(focusListener);
	}

	private IElement lastFocusedElement = null;

	synchronized protected void notifyFocusChanged(IElement element) {
		if ((this.lastFocusedElement == null && element == null)
				|| (this.lastFocusedElement != null && element != null && this.lastFocusedElement
						.toHtml().equals(element.toHtml()))) {
			return;
		}

		for (IFocusListener focusListener : this.focusListeners) {
			focusListener.focusLost(this.lastFocusedElement);
			focusListener.focusGained(element);
		}
		this.lastFocusedElement = element;
	}

	@Override
	public Future<Void> afterCompletion(final String uri) {
		return ExecutorUtil.nonUISyncExec(JQueryEnabledBrowserComposite.class,
				"Active Listeners", new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						JQueryEnabledBrowserComposite.super
								.afterCompletion(uri).get();

						// FIXME: already called in BrowserComposite. But if
						// page has no
						// jQuery included the code does not work. Here we run
						// it again
						// since we know jQuery is included. Code should be made
						// independent from jQuery.
						String js = "window[\"hoveredAnker\"]=null;$(\"body\").bind(\"DOMSubtreeModified beforeunload\",function(){if(window[\"mouseleave\"]&&typeof window[\"mouseleave\"]){window[\"mouseleave\"](window[\"hoveredAnker\"])}});$(\"body\").on({mouseenter:function(){var e=$(this).clone().wrap(\"<p>\").parent().html();window[\"hoveredAnker\"]=e;if(window[\"mouseenter\"]&&typeof window[\"mouseenter\"]){window[\"mouseenter\"](e)}},mouseleave:function(){var e=$(this).clone().wrap(\"<p>\").parent().html();if(window[\"mouseleave\"]&&typeof window[\"mouseleave\"]){window[\"mouseleave\"](e)}}},\"a\")";
						JQueryEnabledBrowserComposite.this.run(js);

						js = "(function(e){e(document).ready(function(){e(\"body\").on(\"focus\",\"*\",function(t){if(t.target!=this)return;var n=e(document.activeElement).clone().wrap(\"<p>\").parent().html();if(window[\"__focus\"]&&typeof window[\"__focus\"]==\"function\"){window[\"__focus\"](n)}})})})(jQuery)";
						JQueryEnabledBrowserComposite.this.run(js);
						return null;
					}
				});
	}

	private IJavaScript getFocusStmt(ISelector selector) {
		return new JavaScript("$('" + selector + "').focus()");
	}

	private IJavaScript getBlurStmt(ISelector selector) {
		return new JavaScript("$('" + selector + "').blur()");
	}

	private IJavaScript getKeyUpStmt(ISelector selector) {
		return new JavaScript("$('" + selector + "').keyup()");
	}

	private IJavaScript getKeyDownStmt(ISelector selector) {
		return new JavaScript("$('" + selector + "').keydown()");
	}

	private IJavaScript getKeyPressStmt(ISelector selector) {
		return new JavaScript("$('" + selector + "').keypress()");
	}

	private IJavaScript getSubmitStmt(ISelector selector) {
		return new JavaScript("$('" + selector + "').closest('form').submit();");
	}

	private IJavaScript getValStmt(ISelector selector, String value) {
		return new JavaScript("$('" + selector + "').val('" + value + "');");
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
	private JavaScript getSimulateKeyPress() {
		return new JavaScript(
				"function simulateKeyPress(e,t){var n=document.createEvent('KeyboardEvent');var r=typeof n.initKeyboardEvent!=='undefined'?'initKeyboardEvent':'initKeyEvent';n[r](t,true,true,window,false,false,false,false,16,0);e.each(function(){this.dispatchEvent(n)})}");
	}

	private JavaScript getForceKeyPressStmt(ISelector selector) {
		return new JavaScript(this.getSimulateKeyPress(), new JavaScript(
				"simulateKeyPress($('" + selector + "'), 'keydown');"),
				new JavaScript("simulateKeyPress($('" + selector
						+ "'), 'keypress');"), new JavaScript(
						"simulateKeyPress($('" + selector + "'), 'keyup');"));
	}

	@Override
	public Future<Boolean> containsElements(ISelector selector) {
		return this.run("return $('" + selector.toString() + "').length > 0;",
				IConverter.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Point> getScrollPosition() {
		return ExecutorUtil.nonUISyncExec(JQueryEnabledBrowserComposite.class,
				"Get Scroll Position", new Callable<Point>() {
					@Override
					public Point call() throws Exception {
						try {
							return JQueryEnabledBrowserComposite.this
									.run("return [jQuery(document).scrollLeft(),jQuery(document).scrollTop()];",
											IConverter.CONVERTER_POINT).get();
						} catch (Exception e) {
							LOGGER.error("Error getting scroll position", e);
						}
						return null;
					}
				});
	}

	@Override
	public Future<Point> getScrollPosition(final ISelector selector) {
		return ExecutorUtil.nonUISyncExec(JQueryEnabledBrowserComposite.class,
				"Get Scroll Position", new Callable<Point>() {
					@Override
					public Point call() throws Exception {
						try {
							String jQuery = "jQuery('" + selector + "')";
							if (selector instanceof IdSelector) {
								// preferred if id contains special characters
								jQuery = "jQuery(document.getElementById(\""
										+ ((IdSelector) selector).getId()
										+ "\"))";
							}
							if (selector instanceof NameSelector) {
								// preferred if name contains special characters
								jQuery = "jQuery(document.getElementsByName(\""
										+ ((NameSelector) selector).getName()
										+ "\")[0])";
							}
							return JQueryEnabledBrowserComposite.this
									.run("var offset = "
											+ jQuery
											+ ".offset(); return offset ? [offset.left, offset.top] : null;",
											IConverter.CONVERTER_POINT).get();
						} catch (Exception e) {
							LOGGER.error("Error getting scroll position", e);
						}
						return null;
					}
				});
	}

	@Override
	public Future<Point> getRelativePosition(final ISelector selector) {
		return ExecutorUtil.nonUISyncExec(JQueryEnabledBrowserComposite.class,
				"Get Relative Position", new Callable<Point>() {
					@Override
					public Point call() throws Exception {
						try {
							return JQueryEnabledBrowserComposite.this
									.run("var offset = jQuery('"
											+ selector
											+ "').offset();return [offset.left-jQuery(document).scrollLeft(),offset.top-jQuery(document).scrollTop()];",
											IConverter.CONVERTER_POINT).get();
						} catch (Exception e) {
							LOGGER.error("Error scrolling", e);
						}
						return null;
					}
				});
	}

	@Override
	public Future<Boolean> scrollTo(final int x, final int y) {
		return ExecutorUtil.nonUISyncExec(JQueryEnabledBrowserComposite.class,
				"Scroll To", new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						try {
							String script = String
									.format("if(jQuery(document).scrollLeft()!=%d||jQuery(document).scrollTop()!=%d){jQuery.scrollTo({left:'%dpx',top:'%dpx'}, 0);return true;}else{return false;}",
											x, y, x, y);
							return JQueryEnabledBrowserComposite.this.run(
									script, IConverter.CONVERTER_BOOLEAN).get();
						} catch (Exception e) {
							LOGGER.error("Error scrolling", e);
						}
						return null;
					}
				});
	}

	@Override
	public Future<Boolean> scrollTo(Point pos) {
		return this.scrollTo(pos.x, pos.y);
	}

	@Override
	public Future<Boolean> scrollTo(final ISelector selector) {
		return ExecutorUtil.nonUISyncExec(JQueryEnabledBrowserComposite.class,
				"Scroll To", new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						Point pos = JQueryEnabledBrowserComposite.this
								.getScrollPosition(selector).get();
						return JQueryEnabledBrowserComposite.this.scrollTo(pos)
								.get();
					}
				});
	}

	@Override
	public Future<Object> focus(ISelector selector) {
		return this.run(this.getFocusStmt(selector));
	}

	@Override
	public Future<IElement> getFocusedElement() {
		return ExecutorUtil.nonUISyncExec(JQueryEnabledBrowserComposite.class,
				"Get Focused Element", new Callable<IElement>() {
					@Override
					public IElement call() throws Exception {
						try {
							String html = JQueryEnabledBrowserComposite.this
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
		// .getInnerArea(BootstrapEnabledBrowserComposite.this);
		// Point elementRelativePos = BootstrapEnabledBrowserComposite.this
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
		return this.run(new JavaScript(this.getFocusStmt(selector), this
				.getValStmt(selector, text), this
				.getForceKeyPressStmt(selector), this.getBlurStmt(selector)));
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
