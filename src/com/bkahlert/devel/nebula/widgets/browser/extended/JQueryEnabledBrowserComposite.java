package com.bkahlert.devel.nebula.widgets.browser.extended;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.IJavaScript;
import com.bkahlert.devel.nebula.widgets.browser.JavaScript;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.IBrowserCompositeExtension;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.JQueryExtension;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.JQueryScrollToExtension;

public class JQueryEnabledBrowserComposite extends ExtendedBrowserComposite
		implements IJQueryEnabledBrowserComposite {
	private static final Logger LOGGER = Logger
			.getLogger(JQueryEnabledBrowserComposite.class);

	public JQueryEnabledBrowserComposite(Composite parent, int style) {
		super(parent, style, new IBrowserCompositeExtension[] {
				new JQueryExtension(), new JQueryScrollToExtension() });
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
				IBrowserComposite.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Point> getScrollPosition() {
		return ExecutorUtil.nonUIAsyncExec(new Callable<Point>() {
			@Override
			public Point call() throws Exception {
				try {
					return JQueryEnabledBrowserComposite.this
							.run("return [jQuery(document).scrollLeft(),jQuery(document).scrollTop()];",
									CONVERTER_POINT).get();
				} catch (Exception e) {
					LOGGER.error("Error scrolling", e);
				}
				return null;
			}
		});
	}

	@Override
	public Future<Point> getRelativePosition(final ISelector selector) {
		return ExecutorUtil.nonUIAsyncExec(new Callable<Point>() {
			@Override
			public Point call() throws Exception {
				try {
					return JQueryEnabledBrowserComposite.this
							.run("var offset = jQuery('"
									+ selector
									+ "').offset();return [offset.left-jQuery(document).scrollLeft(),offset.top-jQuery(document).scrollTop()];",
									CONVERTER_POINT).get();
				} catch (Exception e) {
					LOGGER.error("Error scrolling", e);
				}
				return null;
			}
		});
	}

	@Override
	public Future<Boolean> scrollTo(final int x, final int y) {
		return ExecutorUtil.nonUIAsyncExec(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					String script = String
							.format("if(jQuery(document).scrollLeft()!=%d||jQuery(document).scrollTop()!=%d){jQuery.scrollTo({left:'%dpx',top:'%dpx'}, 0);return true;}else{return false;}",
									x, y, x, y);
					return JQueryEnabledBrowserComposite.this.run(script,
							CONVERTER_BOOLEAN).get();
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
	public Future<Object> focus(ISelector selector) {
		return this.run(this.getFocusStmt(selector));
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
		// .getInnerArea(JQueryEnabledBrowserComposite.this);
		// Point elementRelativePos = JQueryEnabledBrowserComposite.this
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
