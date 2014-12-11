package com.bkahlert.nebula.utils;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.hamcrest.Description;
import org.hamcrest.core.AnyOf;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * This class circumvents the limitation of the {@link CellLabelProvider} as it
 * allows to retrieve the text and image of an element without the need of a
 * {@link StructuredViewer}.
 * <p>
 * <strong>Warning: The construction of an instance is time consuming. Try to
 * reuse an instance instead of creating a new one each time you want to request
 * a label.</strong>
 *
 * @author bkahlert
 *
 */
public class CellLabelClient {

	public static final CellLabelClient INSTANCE = new CellLabelClient();

	private ViewerCell viewerCell;

	private Object element = null;

	private String text = "";
	private Image image = null;
	private Color background = null;
	private Color foreground = null;
	private Font font = null;
	private StyleRange[] styleRanges = null;

	public CellLabelClient() {

		Mockery context = new Mockery() {
			{
				this.setImposteriser(ClassImposteriser.INSTANCE);
				this.setThreadingPolicy(new Synchroniser());
			}
		};

		this.viewerCell = context.mock(ViewerCell.class);
		context.checking(new Expectations() {
			{
				this.allowing(CellLabelClient.this.viewerCell).getElement();
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return CellLabelClient.this.getElement();
					}

					@Override
					public void describeTo(Description description) {
						return;
					}
				});

				this.allowing(CellLabelClient.this.viewerCell).setText(
						this.with(AnyOf.anyOf(any(String.class),
								aNull(String.class))));
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						CellLabelClient.this.text = (String) invocation
								.getParameter(0);
						return null;
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});
				this.allowing(CellLabelClient.this.viewerCell).getText();
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return CellLabelClient.this.getText();
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				this.allowing(CellLabelClient.this.viewerCell).setImage(
						this.with(AnyOf.anyOf(any(Image.class),
								aNull(Image.class))));
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						CellLabelClient.this.image = (Image) invocation
								.getParameter(0);
						return null;
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});
				this.allowing(CellLabelClient.this.viewerCell).getImage();
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return CellLabelClient.this.getImage();
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				this.allowing(CellLabelClient.this.viewerCell).setBackground(
						this.with(AnyOf.anyOf(any(Color.class),
								aNull(Color.class))));
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						CellLabelClient.this.background = (Color) invocation
								.getParameter(0);
						return null;
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});
				this.allowing(CellLabelClient.this.viewerCell).getBackground();
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return CellLabelClient.this.getBackground();
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				this.allowing(CellLabelClient.this.viewerCell).setForeground(
						this.with(AnyOf.anyOf(any(Color.class),
								aNull(Color.class))));
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						CellLabelClient.this.foreground = (Color) invocation
								.getParameter(0);
						return null;
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});
				this.allowing(CellLabelClient.this.viewerCell).getForeground();
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return CellLabelClient.this.getForeground();
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				this.allowing(CellLabelClient.this.viewerCell).setFont(
						this.with(AnyOf.anyOf(any(Font.class),
								aNull(Font.class))));
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						CellLabelClient.this.font = (Font) invocation
								.getParameter(0);
						return null;
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});
				this.allowing(CellLabelClient.this.viewerCell).getFont();
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return CellLabelClient.this.getFont();
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				this.allowing(CellLabelClient.this.viewerCell).setStyleRanges(
						this.with(AnyOf.anyOf(any(StyleRange[].class),
								aNull(StyleRange[].class))));
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						CellLabelClient.this.styleRanges = (StyleRange[]) invocation
								.getParameter(0);
						return null;
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});
				this.allowing(CellLabelClient.this.viewerCell).getStyleRanges();
				this.will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return CellLabelClient.this.getStyleRanges();
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				this.allowing(CellLabelClient.this.viewerCell).getBounds();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getColumnIndex();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getImageBounds();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getItem();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getTextBounds();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getViewerRow();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getVisualIndex();
				this.will(returnValue(null));
			}
		});
	}

	public void setElement(CellLabelProvider cellLabelProvider, Object element) {
		this.element = element;
		this.text = "";
		this.image = null;
		this.background = null;
		this.image = null;
		this.image = null;
		this.image = null;
		this.image = null;
		cellLabelProvider.update(this.viewerCell);
	}

	public Object getElement() {
		return this.element;
	}

	public StyledString getStyledText() {
		StyledString styledText = new StyledString(this.text);
		if (this.styleRanges != null) {
			for (StyleRange styleRange : this.styleRanges) {
				styledText.setStyle(styleRange.start, styleRange.length,
						Stylers.createFrom(styleRange));
			}
		}
		return styledText;
	}

	public String getText() {
		return this.text;
	}

	public Image getImage() {
		return this.image;
	}

	public Color getBackground() {
		return this.background;
	}

	public Color getForeground() {
		return this.foreground;
	}

	public Font getFont() {
		return this.font;
	}

	public StyleRange[] getStyleRanges() {
		return this.styleRanges;
	}

	@Deprecated
	private CellLabelProvider cellLabelProvider;

	@Deprecated
	public CellLabelClient(CellLabelProvider cellLabelProvider) {
		this();
		this.cellLabelProvider = cellLabelProvider;
	}

	@Deprecated
	public void setElement(Object element) {
		this.setElement(this.cellLabelProvider, element);
	}

}
