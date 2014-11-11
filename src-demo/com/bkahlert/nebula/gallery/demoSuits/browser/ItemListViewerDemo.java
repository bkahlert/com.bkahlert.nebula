package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.colors.RGB;
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
		public RGB getColor(Object object) {
			return null;
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
		parent.setLayout(GridLayoutFactory.fillDefaults().numColumns(3)
				.create());

		final AtomicReference<ItemListViewer> itemListViewerHistory = new AtomicReference<ItemListViewer>();
		List<ISelection> history = new LinkedList<ISelection>();

		ItemList itemList = new ItemList(parent, SWT.NONE);
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
				history.add(event.getSelection());
				itemListViewerHistory.get().setInput(history);
				itemListViewerHistory.get().refresh();
			}
		});

		new Label(parent, SWT.SEPARATOR | SWT.VERTICAL)
		.setLayoutData(GridDataFactory.fillDefaults().create());

		ItemList itemListNoWrap = new ItemList(parent, SWT.NONE
				| SWT.HORIZONTAL);
		itemListNoWrap.setMargin(10);
		itemListNoWrap.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		itemListNoWrap.setSpacing(10);

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
				history.add(event.getSelection());
				itemListViewerHistory.get().setInput(history);
				itemListViewerHistory.get().refresh();
			}
		});

		new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL)
		.setLayoutData(GridDataFactory.fillDefaults().span(3, 1)
				.grab(true, false).create());

		ItemList itemListHistory = new ItemList(parent, SWT.HORIZONTAL);
		itemListHistory.setMargin(0);
		itemListHistory.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).span(3, 1).create());
		final Future<Void> rendering = itemListHistory.setSpacing(5);

		itemListViewerHistory.set(new ItemListViewer(itemListHistory));
		itemListViewerHistory.get().setContentProvider(
				new IStructuredContentProvider() {
					@Override
					public void inputChanged(Viewer viewer, Object oldInput,
							Object newInput) {
					}

					@Override
					public Object[] getElements(Object inputElement) {
						if (inputElement instanceof List<?>) {
							@SuppressWarnings("unchecked")
							List<Object> elements = new ArrayList<>(
									(List<Object>) inputElement);
							Collections.reverse(elements);
							return elements.subList(0,
									Math.min(5, elements.size())).toArray();
						}
						return null;
					}

					@Override
					public void dispose() {
					}
				});
		itemListViewerHistory.get().setLabelProvider(new MyLabelProvider() {
			@Override
			public ButtonOption getOption(Object object) {
				return null;
			}

			@Override
			public RGB getColor(Object object) {
				return RGB.BLACK;
			}

			@Override
			public ButtonSize getSize(Object object) {
				return ButtonSize.EXTRA_SMALL;
			}

			@Override
			public ButtonStyle getStyle(Object object) {
				return ButtonStyle.HORIZONTAL;
			}
		});

		itemListViewerHistory.get().addSelectionChangedListener(
				new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						log("selection changed: "
								+ event.getSelection().toString());
					}
				});

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
