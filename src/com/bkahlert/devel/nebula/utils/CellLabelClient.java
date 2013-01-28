package com.bkahlert.devel.nebula.utils;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.hamcrest.Description;
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
				setImposteriser(ClassImposteriser.INSTANCE);
				setThreadingPolicy(new Synchroniser());
			}
		};

		this.viewerCell = context.mock(ViewerCell.class);
		context.checking(new Expectations() {
			{
				allowing(viewerCell).getElement();
				will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return getElement();
					}

					@Override
					public void describeTo(Description description) {
						return;
					}
				});

				allowing(viewerCell).setText(with(any(String.class)));
				will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						setText((String) invocation.getParameter(0));
						return null;
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				allowing(viewerCell).getText();
				will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return getText();
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				allowing(viewerCell).setImage(with(any(Image.class)));
				will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						setImage((Image) invocation.getParameter(0));
						return null;
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				allowing(viewerCell).getImage();
				will(new Action() {
					@Override
					public Object invoke(Invocation invocation)
							throws Throwable {
						return getImage();
					}

					@Override
					public void describeTo(Description arg0) {
						return;
					}
				});

				allowing(viewerCell).setBackground(with(any(Color.class)));
				allowing(viewerCell).setForeground(with(any(Color.class)));
				allowing(viewerCell).setFont(with(any(Font.class)));
				allowing(viewerCell).setStyleRanges(
						with(any(StyleRange[].class)));

				allowing(viewerCell).getBackground();
				will(returnValue(null));
				allowing(viewerCell).getBounds();
				will(returnValue(null));
				allowing(viewerCell).getColumnIndex();
				will(returnValue(null));
				allowing(viewerCell).getFont();
				will(returnValue(null));
				allowing(viewerCell).getForeground();
				will(returnValue(null));
				allowing(viewerCell).getImage();
				will(returnValue(null));
				allowing(viewerCell).getImageBounds();
				will(returnValue(null));
				allowing(viewerCell).getItem();
				will(returnValue(null));
				allowing(viewerCell).getStyleRanges();
				will(returnValue(null));
				allowing(viewerCell).getTextBounds();
				will(returnValue(null));
				allowing(viewerCell).getViewerRow();
				will(returnValue(null));
				allowing(viewerCell).getVisualIndex();
				will(returnValue(null));
			}
		});
	}

	public void setElement(Object element) {
		this.element = element;
		setText("");
		setImage(null);
		this.cellLabelProvider.update(this.viewerCell);
	}

	public Object getElement() {
		return this.element;
	}

	public String getText() {
		return text;
	}

	protected void setText(String text) {
		this.text = text;
	}

	public Image getImage() {
		return image;
	}

	protected void setImage(Image image) {
		this.image = image;
	}
}
