package com.bkahlert.nebula.widgets.scale;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;

public class OrdinalScale extends BootstrapBrowser {

	public static interface IOrdinalScaleListener {
		public void orderChanged(String[] oldOrdinals, String[] newOrdinals);

		public void valueChanged(String oldValue, String newValue);
	}

	private static final Logger LOGGER = Logger.getLogger(OrdinalScale.class);

	private final List<IOrdinalScaleListener> ordinalScaleListeners = new ArrayList<OrdinalScale.IOrdinalScaleListener>();
	private double margin = 0.0;

	private String[] ordinals;
	private int[] order;

	public OrdinalScale(Composite parent, int style, String... ordinals) {
		super(parent, style | SWT.INHERIT_FORCE);
		Assert.isNotNull(ordinals);
		this.ordinals = ordinals;

		this.deactivateNativeMenu();

		new BrowserFunction(this.getBrowser(), "__orderChanged") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length != 1
						|| !(arguments[0] instanceof Object[])) {
					LOGGER.error("Callback arguments of the wrong format");
					return null;
				}
				arguments = (Object[]) arguments[0];
				OrdinalScale.this.order = new int[arguments.length];
				int i = 0;
				for (Object argument : arguments) {
					int j = Integer.valueOf(argument.toString());
					OrdinalScale.this.order[i] = j;
					i++;
				}

				OrdinalScale.this.fireOrderChanged(OrdinalScale.this.ordinals,
						OrdinalScale.this.getOrdinals());
				return null;
			}
		};

		try {
			this.open(
					BrowserUtils.getFileUrl(OrdinalScale.class,
							"html/index.html", "?internal=true"), 60000).get();
			this.injectPlugins().get();
			StringBuilder css = new StringBuilder();
			css.append("label { padding-left: .5em; } ");
			css.append("body.dragging, body.dragging * { cursor: move !important; } ");
			css.append(".dragged { position: absolute; opacity: 0.5; z-index: 2000; } ");
			css.append("ol { padding-left: 0; border: 1px solid #f00; } ");
			css.append("ol li .glyphicon { font-size: 0.8em; } ");
			css.append("ol li.placeholder { position: relative; } ");
			css.append(".glyphicon-move { margin-right: .6em; } ");
			css.append("label { padding-left: .5em; } ");
			css.append("ol li.placeholder:before { position: absolute; left: 0; top: 0; width: 10px; height: 10px; border: 1px solid #f00;");
			this.injectCss(css.toString());
			this.renderOrdinals(ordinals).get();
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	private Future<Boolean> injectPlugins() {
		return this.inject(BrowserUtils.getFileUrl(OrdinalScale.class,
				"html/js/jquery-sortable.js"));
	}

	private Future<Void> renderOrdinals(final String[] ordinals) {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				OrdinalScale.this.order = new int[ordinals.length];
				StringBuilder sb = new StringBuilder("<ol>");
				for (int i = 0; i < ordinals.length; i++) {
					String ordinal = ordinals[i];
					sb.append("<li><i class='glyphicon glyphicon-move'></i><input type='radio' name='value' id='value-"
							+ i
							+ "' value='"
							+ ordinal
							+ "'><label for='value-"
							+ i
							+ "'>"
							+ ordinal
							+ "</label></li>");
					OrdinalScale.this.order[i] = i;
				}
				sb.append("</ol>");
				OrdinalScale.this.setBodyHtml(sb.toString()).get();
				OrdinalScale.this
						.run("$('ol').sortable({ handle: '.glyphicon-move', onDrop: function($item, targetContainer, _super) { $item.removeClass('dragged').removeAttr('style'); $('body').removeClass('dragging'); var order = []; $('input[type=radio]').each(function() { var id = $(this).attr('id'); order.push(id.substr(id.indexOf('string-'))) }); window.__orderChanged(order); } });")
						.get();
				return null;
			}
		});
	}

	private void fireOrderChanged(String[] oldOrdinals, String[] newOrdinals) {
		for (IOrdinalScaleListener ordinalScaleListener : this.ordinalScaleListeners) {
			ordinalScaleListener.orderChanged(oldOrdinals, newOrdinals);
		}
	}

	public String[] getOrdinals() {
		String[] ordinals = new String[this.ordinals.length];
		for (int i = 0; i < ordinals.length; i++) {
			ordinals[this.order[i]] = this.ordinals[i];
		}
		return ordinals;
	}

	public Future<Void> setMargin(double pixels) {
		this.margin = pixels;
		return this.updateLayout();
	}

	private Future<Void> updateLayout() {
		return this.injectCss("body { margin: " + this.margin
				+ "px !important; padding: 0 !important; }");
	}

	public void addListener(IOrdinalScaleListener ordinalScaleListener) {
		this.ordinalScaleListeners.add(ordinalScaleListener);
	}

	public void removeListener(IOrdinalScaleListener ordinalScaleListener) {
		this.ordinalScaleListeners.remove(ordinalScaleListener);
	}
}
