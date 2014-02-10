package com.bkahlert.nebula.gallery.demoSuits.browser.timeline;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.viewer.timeline.impl.MinimalTimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.impl.TimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl.TimelineProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineFactory;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimePassed;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.impl.ZoomStep;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IHotZone;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.model.IZoomStep;
import com.bkahlert.devel.nebula.widgets.timeline.model.Unit;
import com.bkahlert.nebula.datetime.CalendarRange;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

@Demo
public class TimelineGroupViewerDemo extends AbstractDemo {

	@SuppressWarnings("serial")
	public ITimelineInput getInput() {
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
		return input;
	}

	private TimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI> viewer = null;
	private static List<URI> input = new ArrayList<URI>();

	@Override
	public void createControls(Composite composite) {
		Button setInputButton = new Button(composite, SWT.PUSH);
		setInputButton.setText("Set Input");
		setInputButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					input = new ArrayList<URI>(Arrays.asList(new URI(
							"whatever2")));
					final TimePassed passed = new TimePassed(true);
					TimelineGroupViewerDemo.this.viewer.setInput(input);
					final Future<Void> future = TimelineGroupViewerDemo.this.viewer
							.refresh(null);
					ExecUtils.nonUIAsyncExec(MinimalTimelineGroupViewer.class,
							"Waiting for timeline show to complete",
							new Callable<Void>() {
								@Override
								public Void call() throws Exception {
									try {
										future.get();
										log("Input #2 set/refreshed within "
												+ passed.getTimePassed() + "ms");
									} catch (Exception e) {
										log(e.getMessage());
									}
									return null;
								}
							});
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});

		Button addInputElementButton = new Button(composite, SWT.PUSH);
		addInputElementButton.setText("Add Input Element");
		addInputElementButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					input.add(new URI("_"
							+ new BigInteger(130, new SecureRandom())
									.toString(32)));
					final TimePassed passed = new TimePassed(true);
					TimelineGroupViewerDemo.this.viewer.setInput(input);
					final Future<Void> future = TimelineGroupViewerDemo.this.viewer
							.refresh(null);
					ExecUtils.nonUIAsyncExec(MinimalTimelineGroupViewer.class,
							"Waiting for timeline show to complete",
							new Callable<Void>() {
								@Override
								public Void call() throws Exception {
									try {
										future.get();
										log("Random input element added within "
												+ passed.getTimePassed() + "ms");
									} catch (Exception e) {
										log(e.getMessage());
									}
									return null;
								}
							});
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});

		Button highlightButton = new Button(composite, SWT.PUSH);
		highlightButton.setText("Highlight");
		highlightButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Calendar calender = Calendar.getInstance();
				Map<URI, CalendarRange[]> groupedRanges = new HashMap<URI, CalendarRange[]>();
				for (URI uri : input) {
					Calendar calenderEnd = (Calendar) calender.clone();
					calenderEnd.add(Calendar.MINUTE, 1);

					Calendar calender2Start = (Calendar) calenderEnd.clone();
					calender2Start.add(Calendar.MINUTE, 2);

					Calendar calender2End = (Calendar) calender2Start.clone();
					calender2End.add(Calendar.MINUTE, 3);

					groupedRanges.put(uri, new CalendarRange[] {
							new CalendarRange(calender, calenderEnd),
							new CalendarRange(calender2Start, calender2End) });
				}
				TimelineGroupViewerDemo.this.viewer.highlight(groupedRanges,
						null);
				log("Highlight finished");
			}
		});
	}

	@Override
	public void createDemo(Composite composite) {
		final TimelineGroup<ITimeline, URI> timelineGroup = new TimelineGroup<ITimeline, URI>(
				composite, SWT.NONE, new ITimelineFactory<ITimeline>() {
					@Override
					public ITimeline createTimeline(Composite parent, int style) {
						return new Timeline(parent, style);
					}
				});
		this.viewer = new TimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI>(
				timelineGroup,
				new ITimelineProviderFactory<MinimalTimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI>, TimelineGroup<ITimeline, URI>, ITimeline, URI>() {
					@Override
					public ITimelineProvider<MinimalTimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI>, TimelineGroup<ITimeline, URI>, ITimeline, URI> createTimelineProvider() {
						return new TimelineProvider<MinimalTimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI>, TimelineGroup<ITimeline, URI>, ITimeline, URI>(
								new ITimelineLabelProvider<ITimeline>() {

									@Override
									public String getTitle(ITimeline timeline) {
										return TimelineGroupViewerDemo.class
												.getSimpleName();
									}

									@Override
									public Calendar getCenterStart(
											ITimeline timeline) {
										return TimelineGroupViewerDemo.this
												.getInput().getStart();
									}

									@Override
									public Float getTapeImpreciseOpacity(
											ITimeline timeline) {
										return 0.5f;
									}

									@Override
									public Integer getIconWidth(
											ITimeline timeline) {
										return 32;
									}

									@Override
									public String[] getBubbleFunction(
											ITimeline timeline) {
										return null;
									}

									@Override
									public IHotZone[] getHotZones(
											ITimeline timeline) {
										return null;
									}

									@Override
									public IDecorator[] getDecorators(
											ITimeline timeline) {
										return null;
									}

									@Override
									public IZoomStep[] getZoomSteps(
											ITimeline timeline) {
										return new IZoomStep[] { new ZoomStep(
												100, Unit.MINUTE, 2) };
									}

									@Override
									public Integer getZoomIndex(
											ITimeline timeline) {
										return 0;
									}

									@Override
									public Float getTimeZone(ITimeline timeline) {
										return 0f;
									}
								},
								new ArrayList<IBandGroupProvider<MinimalTimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI>, TimelineGroup<ITimeline, URI>, ITimeline, URI>>(
										Arrays.asList(new IBandGroupProvider<MinimalTimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI>, TimelineGroup<ITimeline, URI>, ITimeline, URI>() {
											@Override
											public ITimelineContentProvider<MinimalTimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI>, TimelineGroup<ITimeline, URI>, ITimeline, URI> getContentProvider() {
												return new ITimelineContentProvider<MinimalTimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI>, TimelineGroup<ITimeline, URI>, ITimeline, URI>() {

													@Override
													public boolean isValid(
															URI input) {
														return true;
													}

													@Override
													public void inputChanged(
															MinimalTimelineGroupViewer<TimelineGroup<ITimeline, URI>, ITimeline, URI> timelineGroupViewer,
															URI oldInput,
															URI newInput) {
													}

													@Override
													public Object[] getBands(
															IProgressMonitor monitor) {
														List<Object> bands = new ArrayList<Object>();
														bands.addAll(TimelineGroupViewerDemo.this
																.getInput()
																.getBands());
														bands.addAll(TimelineGroupViewerDemo.this
																.getInput()
																.getBands());
														return bands.toArray();
													}

													@Override
													public Object[] getEvents(
															Object band,
															IProgressMonitor monitor) {
														return ((ITimelineBand) band)
																.getEvents()
																.toArray();
													}
												};
											}

											@Override
											public ITimelineBandLabelProvider getBandLabelProvider() {
												return new ITimelineBandLabelProvider() {

													@Override
													public Boolean isShowInOverviewBands(
															Object band) {
														return true;
													}

													@Override
													public String getTitle(
															Object band) {
														return band.toString();
													}

													@Override
													public Float getRatio(
															Object band) {
														return null;
													}
												};
											}

											@Override
											public ITimelineEventLabelProvider getEventLabelProvider() {
												return new ITimelineEventLabelProvider() {

													@Override
													public boolean isResizable(
															Object event) {
														return false;
													}

													@Override
													public String getTooltip(
															Object event) {
														return "TOOLTIP: "
																+ ((ITimelineEvent) event)
																		.getTooltip();
													}

													@Override
													public String getTitle(
															Object event) {
														return ((ITimelineEvent) event)
																.getTitle();
													}

													@Override
													public Calendar getStart(
															Object event) {
														return ((ITimelineEvent) event)
																.getStart();
													}

													@Override
													public URI getImage(
															Object event) {
														return ((ITimelineEvent) event)
																.getImage();
													}

													@Override
													public URI getIcon(
															Object event) {
														return ((ITimelineEvent) event)
																.getIcon();
													}

													@Override
													public Calendar getEnd(
															Object event) {
														return ((ITimelineEvent) event)
																.getEnd();
													}

													@Override
													public RGB[] getColors(
															Object event) {
														return new RGB[] { ColorUtils
																.getRandomRGB() };
													}

													@Override
													public String[] getClassNames(
															Object event) {
														return new String[0];
													}
												};
											}

										})));
					}
				});

		try {
			input.add(new URI("whatever"));
			this.viewer.setInput(input);
			final Future<Void> future = this.viewer.refresh(null);
			ExecUtils.nonUISyncExec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						future.get();
						log("Refresh successful");
					} catch (Exception e) {
						log(e.getMessage());
					}
					return null;
				}
			});
		} catch (Exception e) {
			log(e.getMessage());
		}
	}
}
