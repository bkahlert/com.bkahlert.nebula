package com.bkahlert.nebula.widgets.scale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.dialogs.RenameDialog;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;

public class OrdinalScale extends BootstrapBrowser {

	public static enum EditType {
		CHANGE_ORDER, CHANGE_VALUE, BOTH;
	}

	public static interface IOrdinalScaleListener {
		public void orderChanged(String[] oldOrdinals, String[] newOrdinals);

		public void valueChanged(String oldValue, String newValue);

		public void ordinalAdded(String newOrdinal);

		public void ordinalRemoved(String ordinal);

		public void ordinalRenamed(String oldName, String newName);
	}

	public static class OrdinalScaleAdapter implements IOrdinalScaleListener {
		@Override
		public void orderChanged(String[] oldOrdinals, String[] newOrdinals) {
		}

		@Override
		public void valueChanged(String oldValue, String newValue) {
		}

		@Override
		public void ordinalAdded(String newOrdinal) {
		}

		@Override
		public void ordinalRemoved(String ordinal) {
		}

		@Override
		public void ordinalRenamed(String oldName, String newName) {
		}
	}

	private static final Logger LOGGER = Logger.getLogger(OrdinalScale.class);

	public static String UNSET_LABEL = "[unset]";

	private final List<IOrdinalScaleListener> ordinalScaleListeners = new ArrayList<OrdinalScale.IOrdinalScaleListener>();
	private EditType editType;
	private double margin = 0.0;

	private String[] ordinals;
	private Integer[] order;
	private String value;

	public OrdinalScale(Composite parent, int style, EditType editType) {
		super(parent, style | SWT.INHERIT_FORCE);
		this.deactivateNativeMenu();
		this.deactivateTextSelections();

		this.editType = editType;

		new BrowserFunction(this.getBrowser(), "__orderChanged") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length != 1
						|| !(arguments[0] instanceof Object[])) {
					LOGGER.error("Callback arguments of the wrong format");
					return null;
				}
				arguments = (Object[]) arguments[0];
				OrdinalScale.this.order = new Integer[arguments.length];
				int i = 0;
				for (Object argument : arguments) {
					int j = Integer.valueOf(argument.toString());
					OrdinalScale.this.order[i] = j;
					i++;
				}

				OrdinalScale.this.fireOrderChangedAdded(
						OrdinalScale.this.ordinals,
						OrdinalScale.this.getOrdinals());
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "__valueChanged") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length != 1 || arguments[0] == null) {
					LOGGER.error("Callback arguments of the wrong format");
					return null;
				}
				String newValue = arguments[0].toString();
				if (newValue.equals(UNSET_LABEL)) {
					newValue = null;
				}
				OrdinalScale.this.fireValueChanged(newValue);
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "__add") {
			@Override
			public Object function(Object[] arguments) {
				try {
					RenameDialog renameDialog = new RenameDialog(
							OrdinalScale.this.getShell(), "");
					renameDialog.create();
					if (renameDialog.open() == Window.OK) {
						OrdinalScale.this.addOrdinal(renameDialog.getCaption());
					}
				} catch (Exception e) {
					LOGGER.error("Error creating nominal value", e);
				}
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "__rename") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length != 1 || arguments[0] == null) {
					LOGGER.error("Callback arguments of the wrong format");
					return null;
				}

				try {
					int i = Integer.valueOf(arguments[0].toString());
					int curr_i = OrdinalScale.this.order[i];

					RenameDialog renameDialog = new RenameDialog(
							OrdinalScale.this.getShell(),
							OrdinalScale.this.ordinals[curr_i]);
					renameDialog.create();
					if (renameDialog.open() == Window.OK) {
						OrdinalScale.this.renameOrdinal(curr_i,
								renameDialog.getCaption());
					}
				} catch (Exception e) {
					LOGGER.error("Error creating nominal value", e);
				}
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "__remove") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length != 1 || arguments[0] == null) {
					LOGGER.error("Callback arguments of the wrong format");
					return null;
				}

				int i = Integer.valueOf(arguments[0].toString());

				OrdinalScale.this.removeOrdinal(OrdinalScale.this.order[i]);
				return null;
			}
		};

		try {
			final Future<Boolean> opened = this.open(BrowserUtils.getFileUrl(
					OrdinalScale.class, "html/index.html", "?internal=true"),
					60000);
			ExecUtils.nonUIAsyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						opened.get();
						OrdinalScale.this.inject(
								BrowserUtils.getFileUrl(OrdinalScale.class,
										"html/js/jquery-sortable.js")).get();
						StringBuilder css = new StringBuilder();
						css.append("body.dragging, body.dragging * { cursor: move !important; } ");
						css.append(".dragged { position: absolute; opacity: 0.5; z-index: 2000; } ");
						css.append("ol { padding-left: 0; border: 1px solid transparent; /* otherwise radio buttons are cut off on Mac OS */ } ");
						css.append("ol li .glyphicon { font-size: 0.8em; } ");
						css.append("ol li .action { display: none; } ");
						css.append("ol li:hover .action { display: inline-block; } ");
						css.append("ol li.placeholder { position: relative; } ");
						css.append("ol li > * { margin-right: .5em; } ");
						css.append("input + label { margin-left: .5em; } "); // FIXME
																				// input
																				// should
																				// render
																				// margin,
																				// making
																				// this
																				// rule
																				// not
																				// necessary
						css.append("ol li.placeholder:before { position: absolute; left: 0; top: 0; width: 10px; height: 10px; border: 1px solid #f00;");
						OrdinalScale.this.injectCss(css.toString());
					} catch (Exception e) {
						LOGGER.error(e);
					}
				}
			});
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	private Future<Void> renderOrdinals() {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				StringBuilder sb = new StringBuilder("<ol>");

				if (OrdinalScale.this.editType != EditType.CHANGE_ORDER) {
					sb.append("<li>");
					if (OrdinalScale.this.editType != EditType.CHANGE_VALUE) {
						sb.append(OrdinalScale.this.editType != EditType.CHANGE_VALUE ? "<i class='glyphicon glyphicon-ban-circle'></i>"
								: "");
					}
					sb.append("<input type='radio' name='value' id='null' value='null' checked");
					if (OrdinalScale.this.editType != EditType.CHANGE_ORDER) {
						sb.append("/>");
					} else {
						sb.append(" style='display: none;'/>");
					}
					sb.append("<label for='null'><small><em>" + UNSET_LABEL
							+ "</em></small></label>");
					sb.append("</li>");
				}

				for (int i = 0; i < OrdinalScale.this.ordinals.length; i++) {
					String ordinal = OrdinalScale.this.ordinals[i];
					sb.append("<li>");
					if (OrdinalScale.this.editType != EditType.CHANGE_VALUE) {
						sb.append(OrdinalScale.this.editType != EditType.CHANGE_VALUE ? "<i class='glyphicon glyphicon-move'></i>"
								: "");
					}
					sb.append("<input type='radio' name='value' id='value-" + i
							+ "' value='" + ordinal + "'");
					if (OrdinalScale.this.editType != EditType.CHANGE_ORDER) {
						sb.append("/>");
					} else {
						sb.append(" style='display: none;'/>");
					}
					sb.append("<label for='value-" + i + "'>" + ordinal
							+ "</label>");
					if (OrdinalScale.this.editType != EditType.CHANGE_VALUE) {
						sb.append("<button type='button' class='btn btn-primary btn-xs action' onclick='var id=$(this).parent().find(\"input\").attr(\"id\"); __rename(id.substr(id.indexOf(\"string-\")));'><small>↩</small></button>");
						sb.append("<button type='button' class='btn btn-danger btn-xs action' onclick='var id=$(this).parent().find(\"input\").attr(\"id\"); __remove(id.substr(id.indexOf(\"string-\")));'><small>⌫</small></button>");
					}
					sb.append("</li>");
				}
				if (OrdinalScale.this.editType != EditType.CHANGE_VALUE) {
					sb.append("<li><button type='button' class='btn btn-default btn-xs' onclick='__add()'>Add Ordinal...</button></li>");
				}
				sb.append("</ol>");
				OrdinalScale.this.setBodyHtml(sb.toString()).get();

				if (OrdinalScale.this.editType != EditType.CHANGE_VALUE) {
					OrdinalScale.this
							.run("$('ol').sortable({ handle: '.glyphicon-move', onDrop: function($item, targetContainer, _super) { $item.removeClass('dragged').removeAttr('style'); $('body').removeClass('dragging'); var order = []; $('[type=radio]').each(function() { var id = $(this).attr('id'); if(id != 'null') order.push(id.substr(id.indexOf('string-'))) }); window.__orderChanged(order); } });")
							.get();
				}
				if (OrdinalScale.this.editType != EditType.CHANGE_ORDER) {
					OrdinalScale.this
							.run("$('ol li [type=radio]').change(function() { __valueChanged($(this).parent().text()); });")
							.get();
				}
				return null;
			}
		});
	}

	private void fireOrderChangedAdded(final String[] oldOrdinals,
			final String[] newOrdinals) {
		ExecUtils.nonUISyncExec(new Runnable() {
			@Override
			public void run() {
				for (IOrdinalScaleListener ordinalScaleListener : OrdinalScale.this.ordinalScaleListeners) {
					ordinalScaleListener.orderChanged(oldOrdinals, newOrdinals);
				}
			}
		});
	}

	private void fireOrdinalAdded(final String newOrdinal) {
		ExecUtils.nonUISyncExec(new Runnable() {
			@Override
			public void run() {
				for (IOrdinalScaleListener ordinalScaleListener : OrdinalScale.this.ordinalScaleListeners) {
					ordinalScaleListener.ordinalAdded(newOrdinal);
				}
			}
		});
	}

	private void fireOrdinalRemoved(final String ordinal) {
		ExecUtils.nonUISyncExec(new Runnable() {
			@Override
			public void run() {
				for (IOrdinalScaleListener ordinalScaleListener : OrdinalScale.this.ordinalScaleListeners) {
					ordinalScaleListener.ordinalRemoved(ordinal);
				}
			}
		});
	}

	protected void fireOrdinalRenamed(final String oldName, final String newName) {
		ExecUtils.nonUISyncExec(new Runnable() {
			@Override
			public void run() {
				for (IOrdinalScaleListener ordinalScaleListener : OrdinalScale.this.ordinalScaleListeners) {
					ordinalScaleListener.ordinalRenamed(oldName, newName);
				}
			}
		});
	}

	private void fireValueChanged(final String newValue) {
		if (ObjectUtils.equals(this.value, newValue)) {
			return;
		}
		final String oldValue = this.value;
		this.value = newValue;
		ExecUtils.nonUISyncExec(new Runnable() {
			@Override
			public void run() {
				for (IOrdinalScaleListener ordinalScaleListener : OrdinalScale.this.ordinalScaleListeners) {
					ordinalScaleListener.valueChanged(oldValue, newValue);
				}
			}
		});
	}

	public Future<Void> setOrdinals(String... ordinals) {
		this.ordinals = ordinals;
		OrdinalScale.this.order = new Integer[ordinals.length];
		for (int i = 0; i < ordinals.length; i++) {
			this.order[i] = i;
		}
		return this.renderOrdinals();
	}

	public Future<Void> addOrdinal(final String ordinal) {
		Assert.isNotNull(ordinal);
		List<String> ordinals = new ArrayList<String>(
				Arrays.asList(this.ordinals));
		ordinals.add(ordinal);
		this.ordinals = ordinals.toArray(new String[0]);

		List<Integer> order = new ArrayList<Integer>(Arrays.asList(this.order));
		order.add(order.size());
		this.order = order.toArray(new Integer[0]);

		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				OrdinalScale.this.renderOrdinals().get();
				OrdinalScale.this.fireOrdinalAdded(ordinal);
				return null;
			}
		});
	}

	/**
	 * Removed an element from the orginals. The ordinal is identified by its
	 * current index (starting at 0).
	 * 
	 * @param originalIndex
	 */
	public Future<Void> removeOrdinal(int index) {
		final AtomicReference<String> removed = new AtomicReference<String>();
		final List<String> newOrdinals = new LinkedList<String>();
		for (int i = 0; i < this.ordinals.length; i++) {
			if (i != index) {
				newOrdinals.add(this.ordinals[i]);
			} else {
				removed.set(this.ordinals[i]);
			}
		}
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				OrdinalScale.this.setOrdinals(
						newOrdinals.toArray(new String[0])).get();
				OrdinalScale.this.fireOrdinalRemoved(removed.get());
				return null;
			}
		});
	}

	/**
	 * Removed an element from the orginals. The ordinal is identified by its
	 * current index (starting at 0).
	 * 
	 * @param newName
	 * 
	 * @param originalIndex
	 */
	public Future<Void> renameOrdinal(int index, final String newName) {
		final String oldName = this.ordinals[index];
		this.ordinals[index] = newName;
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				OrdinalScale.this.setOrdinals(OrdinalScale.this.ordinals).get();
				OrdinalScale.this.fireOrdinalRenamed(oldName, newName);
				return null;
			}
		});
	}

	public String[] getOrdinals() {
		String[] ordinals = new String[this.ordinals.length];
		for (int i = 0; i < ordinals.length; i++) {
			ordinals[i] = this.ordinals[this.order[i]];
		}
		return ordinals;
	}

	public Future<Void> setValue(final String ordinal) {
		final String label = ordinal == null ? UNSET_LABEL : ordinal;
		final String js = "$('li [type=radio]').each(function() { $this = $(this); if($this.parent().text() == '"
				+ label
				+ "') { $this.prop('checked', true); } else { $this.prop('checked', false); } }); if($('li [type=radio]').filter(function() { return $(this).prop('checked'); }).length == 0) $('#null').prop('checked', true);";
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				OrdinalScale.this.run(js, IConverter.CONVERTER_VOID).get();
				OrdinalScale.this.fireValueChanged(ordinal);
				return null;
			}
		});
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point xx = super.computeSize(wHint, hHint, changed);
		System.err.println(xx);
		xx.x += 100;
		xx.y += 100;
		return xx;
	}

	public String getValue() {
		return this.value;
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
