package com.bkahlert.nebula.gallery.demoSuits.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
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
import com.bkahlert.nebula.viewer.StyledLabelProvider;

@Demo
public class StylerUtilsDemo extends AbstractDemo {

	public static StyledString FANCY_STYLED_STRING = new StyledString("Lorem ",
			Stylers.DEFAULT_STYLER).append("ipsum ", Stylers.BOLD_STYLER)
			.append("dolor ", Stylers.COUNTER_STYLER)
			.append("sit ", Stylers.SMALL_STYLER)
			.append("amet, ", Stylers.MINOR_STYLER)
			.append("consectetur", Stylers.ATTENTION_STYLER)
			.append(" adipiscing ", Stylers.IMPORTANCE_HIGH_STYLER)
			.append("elit.", Stylers.IMPORTANCE_LOW_STYLER);

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

		data.add(new StyledString("Custom Designs", null));
		{
			StyledString string = new StyledString("Lorem ipsum");
			string.append("  (", Stylers.MINOR_STYLER);
			Stylers.append(string, new StyledString("minor, minor, ").append(
					"minor bold", Stylers.BOLD_STYLER));
			string.append(")", Stylers.MINOR_STYLER);
			data.add(string);
		}
		{
			StyledString string = new StyledString("Lorem ipsum");
			string.append("  (", Stylers.MINOR_STYLER);
			Stylers.append(
					string,
					new StyledString("minor, minor, ")
							.append("minor bold", Stylers.BOLD_STYLER)
							.append(", ")
							.append("mini",
									Stylers.combine(Stylers.MINOR_STYLER,
											Stylers.ITALIC_STYLER,
											Stylers.BOLD_STYLER)));
			string.append(")", Stylers.MINOR_STYLER);
			data.add(string);
		}

		if (true) {
			data.add(new StyledString("Simple Stylers", null));
			for (Pair<String, Styler> styler : stylers) {
				data.add(new StyledString(styler.getFirst(), styler.getSecond()));
			}

			data.add(new StyledString(
					"Stylers.substring(FANCY_STYLED_STRING, \"s\")", null));
			for (StyledString split : Stylers.split(FANCY_STYLED_STRING, "s")) {
				data.add(split);
			}

			data.add(new StyledString("Stylers.applyStyler(3,12)", null));
			for (Pair<String, Styler> styler : stylers) {
				for (Pair<String, Styler> styler2 : stylers) {
					data.add(Stylers.apply(
							new StyledString(styler.getFirst(), styler
									.getSecond()), styler2.getSecond(), 3, 12)
							.append(new StyledString(
									" - " + styler2.getFirst(), styler2
											.getSecond())));
				}
			}

			data.add(new StyledString("Shortened Stylers", null));
			for (Pair<String, Styler> styler : stylers) {
				data.add(Stylers.shorten(new StyledString(styler.getFirst(),
						styler.getSecond()), styler.getFirst().length() - 10,
						" ..."));
			}

			data.add(new StyledString("Combined Stylers", null));
			for (Pair<String, Styler> styler : stylers) {
				for (Pair<String, Styler> styler2 : stylers) {
					data.add(new StyledString(styler.getFirst() + " and "
							+ styler2.getFirst(), Stylers.combine(
							styler.getSecond(), styler2.getSecond())));
				}
			}

			data.add(new StyledString("Cloned Stylers", null));
			data.add(Stylers.clone(FANCY_STYLED_STRING).append(
					" -- I'm a copy.", Stylers.MINOR_STYLER));
			data.add(FANCY_STYLED_STRING);

			data.add(new StyledString("Appended Stylers", null));
			for (Pair<String, Styler> styler : stylers) {
				data.add(Stylers.append(new StyledString(styler.getFirst(),
						styler.getSecond()).append(" + "), FANCY_STYLED_STRING));
			}

			data.add(new StyledString("ReBased Stylers", null));
			for (Pair<String, Styler> styler : stylers) {
				StyledString string = Stylers.clone(FANCY_STYLED_STRING);
				Stylers.rebase(string, styler.getSecond());
				string.append("        (base: ");
				string.append(styler.getFirst(), styler.getSecond());
				string.append(")");
				data.add(string);
			}

			data.add(new StyledString("Substring Stylers", null));
			for (int i = 0, m = FANCY_STYLED_STRING.length(); i < m; i++) {
				for (int j = i + 1; j <= m; j++) {
					data.add(Stylers.substring(FANCY_STYLED_STRING, i, j));
				}
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
				new DelegatingStyledCellLabelProvider(
						new StyledLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								StyledString string = (StyledString) element;
								if (string.getStyleRanges().length == 0) {
									return new StyledString(string.getString(),
											new Styler() {
												@Override
												public void applyStyles(
														TextStyle textStyle) {
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
									return Display.getCurrent().getSystemColor(
											SWT.COLOR_WHITE);
								} else {
									return null;
								}
							}

							@Override
							public Color getBackground(Object element) {
								StyledString string = (StyledString) element;
								if (string.getStyleRanges().length == 0) {
									return Display.getCurrent().getSystemColor(
											SWT.COLOR_BLACK);
								} else {
									return null;
								}
							}
						}));

		viewer.setInput(data);
		viewer.refresh();
	}
}
