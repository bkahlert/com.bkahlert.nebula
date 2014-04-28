package com.bkahlert.nebula.widgets.jointjs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.timeline.TimelineJsonGenerator;

/**
 * Shows an image in a way that it always fills the {@link Composite}'s
 * available width.
 * 
 * @author bkahlert
 * 
 */
public class JointJS extends Browser {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(JointJS.class);

	public static interface IJointJSListener {
		public void loaded(String json);

		public void save(String json);
	}

	private final List<IJointJSListener> jointJSListeners = new ArrayList<IJointJSListener>();

	public JointJS(Composite parent, int style) {
		super(parent, style);
		this.deactivateNativeMenu();

		new BrowserFunction(this.getBrowser(), "loaded") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1) {
					for (IJointJSListener jointJSListener : JointJS.this.jointJSListeners) {
						jointJSListener.loaded((String) arguments[0]);
					}
				}
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "save") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1) {
					for (IJointJSListener jointJSListener : JointJS.this.jointJSListeners) {
						jointJSListener.save((String) arguments[0]);
					}
				}
				return null;
			}
		};

		this.open(BrowserUtils.getFileUrl(JointJS.class, "html/index.html",
				"?internal=true"), 5000);
	}

	public void load(String json) {
		JointJS.this.run("return com.bkahlert.jointjs.load("
				+ TimelineJsonGenerator.enquote(json) + ")");
	}

	public Future<String> save() {
		return this.run("return com.bkahlert.jointjs.save();",
				IConverter.CONVERTER_STRING);
	}

	public void addJointJSListener(IJointJSListener jointJSListener) {
		this.jointJSListeners.add(jointJSListener);
	}

	public void removeImageListener(IJointJSListener jointJSListener) {
		this.jointJSListeners.remove(jointJSListener);
	}

}
