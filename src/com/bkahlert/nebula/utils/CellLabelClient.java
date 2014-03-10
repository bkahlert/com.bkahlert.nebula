package com.bkahlert.nebula.utils;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
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

import com.bkahlert.nebula.widgets.timeline.impl.TimePassed;

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
	private CellLabelProvider cellLabelProvider;
	private ViewerCell viewerCell;

	private Object element = null;

	private String text = "";
	private Image image = null;

	public CellLabelClient(CellLabelProvider cellLabelProvider) {
		this.cellLabelProvider = cellLabelProvider;

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
						CellLabelClient.this.setText((String) invocation
								.getParameter(0));
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
						CellLabelClient.this.setImage((Image) invocation
								.getParameter(0));
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
				this.allowing(CellLabelClient.this.viewerCell).setForeground(
						this.with(AnyOf.anyOf(any(Color.class),
								aNull(Color.class))));
				this.allowing(CellLabelClient.this.viewerCell).setFont(
						this.with(AnyOf.anyOf(any(Font.class),
								aNull(Font.class))));
				this.allowing(CellLabelClient.this.viewerCell).setStyleRanges(
						this.with(AnyOf.anyOf(any(StyleRange[].class),
								aNull(StyleRange[].class))));

				this.allowing(CellLabelClient.this.viewerCell).getBackground();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getBounds();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getColumnIndex();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getFont();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getForeground();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getImage();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getImageBounds();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getItem();
				this.will(returnValue(null));
				this.allowing(CellLabelClient.this.viewerCell).getStyleRanges();
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

	public void setElement(Object element) {
		TimePassed passed = new TimePassed("Setting element: " + element);
		this.element = element;
		this.setText("");
		this.setImage(null);
		this.cellLabelProvider.update(this.viewerCell);
		passed.finished();
	}

	public Object getElement() {
		return this.element;
	}

	public String getText() {
		return this.text;
	}

	protected void setText(String text) {
		this.text = text;
	}

	public Image getImage() {
		return this.image;
	}

	protected void setImage(Image image) {
		this.image = image;
	}
}
