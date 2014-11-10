package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.util.concurrent.Future;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonOption;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonSize;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonStyle;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.itemlist.ItemListViewer;
import com.bkahlert.nebula.widgets.itemlist.ItemListViewer.IButtonLabelProvider;

@Demo
public class ItemListViewerDemo extends AbstractDemo {

	@SuppressWarnings("unchecked")
	private static <T> T getRandomEnum(Class<T> enumClass) {
		if (enumClass.isEnum()) {
			Object[] values = enumClass.getEnumConstants();
			return (T) values[(int) Math.floor(Math.random() * values.length)];
		}
		return null;
	}

	private static String[] getInput() {
		String[] input = new String[20];
		for (int i = 0; i < input.length; i++) {
			input[i] = "Element #" + i;
		}
		return input;
	}

	private ItemListViewer itemListViewer;
	private ItemListViewer itemListViewerNoWrap;

	@Override
	public void createControls(Composite composite) {
		createControlButton("set input", new Runnable() {
			@Override
			public void run() {
				log("creating input");
				try {
					itemListViewer.setInput(getInput());
					itemListViewerNoWrap.setInput(getInput());
					itemListViewer.refresh();
					itemListViewerNoWrap.refresh();
				} catch (Exception e) {
					log(e);
				}
				log("created input");
			}
		});
	}

	private class MyLabelProvider extends LabelProvider implements
	IButtonLabelProvider {
		@Override
		public String getText(Object element) {
			return element.toString();
		}

		@Override
		public ButtonOption getOption(Object object) {
			return getRandomEnum(ButtonOption.class);
		}

		@Override
		public ButtonSize getSize(Object object) {
			return getRandomEnum(ButtonSize.class);
		}

		@Override
		public ButtonStyle getStyle(Object object) {
			return getRandomEnum(ButtonStyle.class);
		}
	}

	@Override
	public void createDemo(final Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
				.fillDefaults().grab(true, true).create());

		ItemList itemList = new ItemList(parent, SWT.BORDER);
		itemList.setMargin(10);
		itemList.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
				.create());
		itemList.setSpacing(10);

		itemListViewer = new ItemListViewer(itemList);
		itemListViewer.setContentProvider(ArrayContentProvider.getInstance());
		itemListViewer.setLabelProvider(new MyLabelProvider());
		itemListViewer.setInput(getInput());
		itemListViewer.refresh();
		itemListViewer
		.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				log("selection changed: "
						+ event.getSelection().toString());
			}
		});

		new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
				.fillDefaults().grab(true, true).create());

		ItemList itemListNoWrap = new ItemList(parent, SWT.BORDER
				| SWT.HORIZONTAL);
		itemListNoWrap.setMargin(10);
		itemListNoWrap.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		final Future<Void> rendering = itemListNoWrap.setSpacing(10);

		itemListViewerNoWrap = new ItemListViewer(itemListNoWrap);
		itemListViewerNoWrap.setContentProvider(ArrayContentProvider
				.getInstance());
		itemListViewerNoWrap.setLabelProvider(new MyLabelProvider());
		itemListViewerNoWrap.setInput(getInput());
		itemListViewerNoWrap.refresh();
		itemListViewerNoWrap
		.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				log("selection changed: "
						+ event.getSelection().toString());
			}
		});

		new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
				.fillDefaults().grab(true, true).create());

		ExecUtils.nonUIAsyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					rendering.get();
				} catch (Exception e) {
					log(e);
				}
				ExecUtils.asyncExec(new Runnable() {
					@Override
					public void run() {
						parent.layout(true, true);
					}
				});
			}
		});
	}
}
