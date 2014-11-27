package com.bkahlert.nebula.gallery.demoSuits.browser.timeline;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.nebula.widgets.composer.ComposerReadOnly;
import com.bkahlert.nebula.widgets.editor.Editor;
import com.bkahlert.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.nebula.widgets.timeline.impl.Options;
import com.bkahlert.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineEvent;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineInput;

@Demo
public class TimelineAndComposerAndEditorDemo extends AbstractDemo {

	private ComposerReadOnly composerReadOnly = null;
	private Editor<String> editor;

	@SuppressWarnings("serial")
	@Override
	public void createDemo(Composite composite) {
		composite.setLayout(new GridLayout(2, true));

		Timeline timeline = new Timeline(composite, SWT.BORDER);
		timeline.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				2));

		final Calendar start1 = Calendar.getInstance();
		final Calendar end1 = Calendar.getInstance();
		end1.add(Calendar.HOUR, 1);

		final Calendar start2 = Calendar.getInstance();
		final Calendar end2 = Calendar.getInstance();
		start2.add(Calendar.HOUR, 1);
		end2.add(Calendar.HOUR, 2);
		ITimelineInput input = null;
		try {
			input = new TimelineInput(new Options() {
				{
					this.setTimeZone(1.0f);
				}
			}, new ArrayList<ITimelineBand>() {
				{
					this.add(new TimelineBand(new Options(),
							new ArrayList<ITimelineEvent>() {
								{
									this.add(new TimelineEvent(
											"Title 1",
											"Tooltip 1",
											new URI(
													"http://aux4.iconpedia.net/uploads/868888537436999746.png"),
											new URI(
													"http://www.w3.org/html/logo/downloads/HTML5_Badge_512.png"),
											start1, end1, new RGB[] { new RGB(
													0.259, 0.706, 0.902) },
											true, null, "Payload #1"));

									this.add(new TimelineEvent(
											"Title 2",
											"Tooltip 2",
											new URI(
													"http://aux4.iconpedia.net/uploads/868888537436999746.png"),
											new URI(
													"http://www.w3.org/html/logo/downloads/HTML5_Badge_512.png"),
											start2, end2, new RGB[] { new RGB(
													0.559, 0.706, 0.902) },
											true, null, "Payload #2"));
								}
							}));
				}
			});
		} catch (URISyntaxException e) {
			log(e.getMessage());
		}

		timeline.addTimelineListener(new ITimelineListener() {

			@Override
			public void rightClicked(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this
						.customLog("right clicked: " + event);
			}

			@Override
			public void resizing(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this.customLog("resizing: "
						+ event);
			}

			@Override
			public void resized(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this.customLog("resize end: "
						+ event);
			}

			@Override
			public void resizeStarted(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this
						.customLog("resize start: " + event);
			}

			@Override
			public void middleClicked(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this
						.customLog("middle clicked: " + event);
			}

			@Override
			public void hoveredOut(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this.customLog("hovered out: "
						+ event);
			}

			@Override
			public void hoveredIn(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this.customLog("hovered in: "
						+ event);
			}

			@Override
			public void doubleClicked(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this
						.customLog("double clicked: " + event);
			}

			@Override
			public void clicked(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this
						.customLog("left clicked: " + event);
			}

			@Override
			public void selected(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				TimelineAndComposerAndEditorDemo.this.customLog("selected: "
						+ event);
			}
		});
		timeline.show(input, 500, 500, null);

		this.composerReadOnly = new ComposerReadOnly(composite, SWT.NONE);
		this.composerReadOnly.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		this.composerReadOnly.setSource("I'm a "
				+ ComposerReadOnly.class.getSimpleName()
				+ " and you can simply hover over elements in the "
				+ Timeline.class.getSimpleName() + " to the left!");

		this.editor = new Editor<String>(composite, SWT.NONE, 1000,
				ToolbarSet.DEFAULT) {
			@Override
			public String getTitle(String objectToLoad, IProgressMonitor monitor)
					throws Exception {
				return objectToLoad;
			}

			@Override
			public void setHtml(String loadedObject, String html,
					IProgressMonitor monitor) throws Exception {
			}

			@Override
			public String getHtml(String objectToLoad, IProgressMonitor monitor)
					throws Exception {
				return objectToLoad;
			}
		};
		this.editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.editor.load("I'm a " + Editor.class.getSimpleName()
				+ " and change my contents as well.");
	}

	private void customLog(String html) {
		log(html);
		if (this.composerReadOnly != null
				&& !this.composerReadOnly.isDisposed()) {
			this.composerReadOnly.setSource(this.composerReadOnly.getSource()
					+ "<span style=\"font-weight:700;color:"
					+ ColorUtils.getRandomRGB().toHexString() + ";\">" + html
					+ "</span><br/>");
		}

		if (this.editor != null && !this.editor.isDisposed()) {
			this.editor.load("<span style=\"font-style:italic;color:"
					+ ColorUtils.getRandomRGB().toHexString() + ";\">" + html
					+ "</span><br/>");
		}
	}
}
