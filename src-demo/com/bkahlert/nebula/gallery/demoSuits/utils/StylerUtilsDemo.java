package com.bkahlert.nebula.gallery.demoSuits.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.utils.FontUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.viewer.SortableTableViewer;

@Demo
public class StylerUtilsDemo extends AbstractDemo {

	private static class LabelProvider extends ColumnLabelProvider implements
			IStyledLabelProvider {
		@Override
		public StyledString getStyledText(Object element) {
			StyledString string = (StyledString) element;
			if (string.getStyleRanges().length == 0) {
				return new StyledString(string.getString(), new Styler() {
					@Override
					public void applyStyles(TextStyle textStyle) {
						textStyle.font = FontUtils.BOLD_FONT;
					}
				});
			} else {
				return string;
			}
		}

		@Override
		public Color getForeground(Object element) {
			StyledString string = (StyledString) element;
			if (string.getStyleRanges().length == 0) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
			} else {
				return null;
			}
		}

		@Override
		public Color getBackground(Object element) {
			StyledString string = (StyledString) element;
			if (string.getStyleRanges().length == 0) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
			} else {
				return null;
			}
		}
	}

	@Override
	public void createDemo(Composite parent) {
		List<Pair<String, Styler>> stylers = new ArrayList<Pair<String, Styler>>();

		try {
			for (Field f : Stylers.class.getFields()) {
				if (f.getType() == Styler.class) {
					stylers.add(new Pair<String, StyledString.Styler>(
							Styler.class.getSimpleName() + "." + f.getName(),
							(Styler) f.get(null)));
				}
			}
		} catch (Exception e) {
			log("Error reading available " + Styler.class.getSimpleName() + "s");
			log(e);
		}

		final List<StyledString> data = new ArrayList<StyledString>();

		data.add(new StyledString("Complex Stylers", null));
		StyledString fancy = new StyledString("Lorem ", Stylers.DEFAULT_STYLER)
				.append("ipsum ", Stylers.BOLD_STYLER)
				.append("dolor ", Stylers.COUNTER_STYLER)
				.append("sit ", Stylers.SMALL_STYLER)
				.append("amet, ", Stylers.MINOR_STYLER)
				.append("consectetur", Stylers.ATTENTION_STYLER)
				.append(" adipiscing ", Stylers.IMPORTANCE_HIGH_STYLER)
				.append("elit.", Stylers.IMPORTANCE_LOW_STYLER);
		data.add(Stylers.clone(fancy).append(" -- I'm a copy.",
				Stylers.MINOR_STYLER));
		data.add(fancy);
		for (Pair<String, Styler> styler : stylers) {
			data.add(Stylers.append(
					new StyledString(styler.getFirst(), styler.getSecond())
							.append(" + "), fancy));
			data.add(Stylers.append(
					new StyledString(styler.getFirst(), styler.getSecond())
							.append(" + "), fancy, styler.getSecond()));
		}

		data.add(new StyledString("Simple Stylers", null));
		for (Pair<String, Styler> styler : stylers) {
			data.add(new StyledString(styler.getFirst(), styler.getSecond()));
		}

		data.add(new StyledString("Combined Stylers", null));
		for (Pair<String, Styler> styler : stylers) {
			for (Pair<String, Styler> styler2 : stylers) {
				data.add(new StyledString(styler.getFirst() + " and "
						+ styler2.getFirst(), Stylers.combine(
						styler.getSecond(), styler2.getSecond())));
			}
		}

		Table table = new Table(parent, SWT.NONE);

		SortableTableViewer viewer = new SortableTableViewer(table);
		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return data.toArray();
			}
		});

		viewer.createColumn(null, new RelativeWidth(1.0)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(new LabelProvider()));

		viewer.setInput(data);
		viewer.refresh();
	}
}