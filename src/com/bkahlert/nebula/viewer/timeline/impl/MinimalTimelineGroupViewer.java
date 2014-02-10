package com.bkahlert.nebula.viewer.timeline.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimePassed;
import com.bkahlert.devel.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.viewer.timeline.ITimelineGroupViewer;
import com.bkahlert.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.nebula.viewer.timeline.provider.complex.ITimelineProvider.ITimelineLabelProviderCreationInterceptor;
import com.bkahlert.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

/**
 * This class implements a minimal implementation of
 * {@link ITimelineGroupViewer}.
 * <p>
 * {@link ITimeline}s are economically used. They are only created if necessary.
 * No more needed {@link ITimeline}s are used to load another key if no more
 * needed.
 * 
 * @author bkahlert
 * 
 * @param <TIMELINEGROUP>
 * @param <TIMELINE>
 */
public class MinimalTimelineGroupViewer<TIMELINE extends ITimeline, INPUT>
		extends AbstractTimelineGroupViewer<TIMELINE, INPUT> {

	private static final Logger LOGGER = Logger
			.getLogger(MinimalTimelineGroupViewer.class);

	/**
	 * Stores the {@link ITimeline} and the {@link ITimelineProvider} that
	 * provides its demoAreaContent.
	 * 
	 * @author bkahlert
	 * 
	 * @param <TIMELINEGROUPVIEWER>
	 * @param <TIMELINEGROUP>
	 * @param <TIMELINE>
	 * @param <INPUT>
	 */
	private static class Asset<TIMELINE extends IBaseTimeline, INPUT> {
		private final TIMELINE timeline;
		private final ITimelineProvider<TIMELINE, INPUT> timelineProvider;

		public Asset(TIMELINE timeline,
				ITimelineProvider<TIMELINE, INPUT> timelineProvider) {
			super();
			this.timeline = timeline;
			this.timelineProvider = timelineProvider;
		}

		public TIMELINE getTimeline() {
			return this.timeline;
		}

		public ITimelineProvider<TIMELINE, INPUT> getTimelineProvider() {
			return this.timelineProvider;
		}
	}

	/**
	 * Tries to find out if the rawInput consists of multiple objects.<br>
	 * Returns an array of the found objects.
	 * 
	 * @param rawInput
	 *            if no objects could be found the result only contains the
	 *            rawInput; null if rawInput was null
	 * @return
	 */
	public static Object[] breakUpAndRemoveDuplicates(Object input) {
		if (input == null) {
			return new Object[0];
		}
		Set<Object> objects = new LinkedHashSet<Object>(); // keeps order
		if (input.getClass().isArray()) {
			objects.addAll(Arrays.asList((Object[]) input));
		} else if (input instanceof Collection<?>) {
			objects.addAll((Collection<?>) input);
		} else {
			objects.add(input);
		}
		return objects.toArray();
	}

	/**
	 * Restores {@link IOptions} from and old to a new {@link IOptions} in a way
	 * the on refreshing the {@link IBaseTimeline} receives its new data but
	 * keeps its viewport.
	 * 
	 * @param oldOptions
	 * @param newOptions
	 */
	public static void restoreOptions(IOptions oldOptions, IOptions newOptions) {
		if (oldOptions != null && newOptions != null) {
			newOptions.setCenterStart(oldOptions.getCenterStart());
			newOptions.setPermanentDecorators(oldOptions
					.getPermanentDecorators());
			newOptions.setHotZones(oldOptions.getHotZones());
		}
	}

	private final ITimelineProviderFactory<TIMELINE, INPUT> timelineProviderFactory;
	private Object rawInput;

	/**
	 * Contains completely loaded keys and their {@link Asset}s.
	 */
	private final Map<INPUT, Asset<TIMELINE, INPUT>> loadedKeys = new HashMap<INPUT, Asset<TIMELINE, INPUT>>();

	public MinimalTimelineGroupViewer(
			TimelineGroup<TIMELINE, INPUT> timelineGroup,
			ITimelineProviderFactory<TIMELINE, INPUT> timelineProviderFactory) {
		super(timelineGroup);
		Assert.isNotNull(timelineProviderFactory);
		this.timelineProviderFactory = timelineProviderFactory;
	}

	@Override
	public void setInput(final Object rawInput) {
		if (this.rawInput != rawInput) {
			this.rawInput = rawInput;
		}
	}

	@Override
	public Object getInput() {
		return this.rawInput;
	}

	/**
	 * Notifies a {@link ITimelineProvider} about a changed input
	 * 
	 * @param timelineProvider
	 * @param oldInput
	 * @param newInput
	 */
	protected void notifyInputChanged(
			ITimelineProvider<TIMELINE, INPUT> timelineProvider,
			INPUT oldInput, INPUT newInput) {
		for (IBandGroupProvider<INPUT> bandGroupProvider : timelineProvider
				.getBandGroupProviders()) {
			bandGroupProvider.getContentProvider().inputChanged(this, oldInput,
					newInput);
		}
	}

	@Override
	public void update(Object object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Future<Void> refresh(final IProgressMonitor monitor) {
		return ExecUtils.nonUISyncExec(MinimalTimelineGroupViewer.class,
				"Refresh", new Callable<Void>() {
					@SuppressWarnings("unchecked")
					@Override
					public Void call() throws Exception {
						final TimelineGroup<TIMELINE, INPUT> timelineGroup = MinimalTimelineGroupViewer.this
								.getControl();

						final TimePassed passed = new TimePassed(
								"TIMELINE REFRESH");
						List<Object> neededKeys = new ArrayList<Object>(
								Arrays.asList(breakUpAndRemoveDuplicates(MinimalTimelineGroupViewer.this.rawInput)));

						final SubMonitor subMonitor = SubMonitor.convert(
								monitor, neededKeys.size());

						final List<TIMELINE> recyclableTimelines = new ArrayList<TIMELINE>();
						for (Iterator<Entry<INPUT, Asset<TIMELINE, INPUT>>> iterator = MinimalTimelineGroupViewer.this.loadedKeys
								.entrySet().iterator(); iterator.hasNext();) {
							final Entry<INPUT, Asset<TIMELINE, INPUT>> loaded = iterator
									.next();
							if (!neededKeys.contains(loaded.getKey())) {
								ITimelineProvider<TIMELINE, INPUT> provider = loaded
										.getValue().getTimelineProvider();
								final TIMELINE timeline = loaded.getValue()
										.getTimeline();
								ExecUtils.syncExec(new Runnable() {
									@Override
									public void run() {
										timeline.setData(null);
										recyclableTimelines.add(timeline);
									}
								});
								MinimalTimelineGroupViewer.this
										.notifyInputChanged(provider, null,
												null);
								iterator.remove();
							}
						}

						passed.tell("calculated recycleable timelines");

						/*
						 * Invariant: (1) loadedKeys only contains actually
						 * needed and loaded key (2) recyclableTimelines
						 * contains timelines that can be reused
						 */
						List<INPUT> unpreparedKeys = new ArrayList<INPUT>(
								CollectionUtils
										.subtract(
												neededKeys,
												MinimalTimelineGroupViewer.this.loadedKeys
														.keySet()));

						if (unpreparedKeys.size() > recyclableTimelines.size()) {
							int missingTimelines = unpreparedKeys.size()
									- recyclableTimelines.size();
							for (int i = 0; i < missingTimelines; i++) {
								try {
									TIMELINE timeline = ExecUtils
											.syncExec(new Callable<TIMELINE>() {
												@Override
												public TIMELINE call()
														throws Exception {
													return timelineGroup
															.createTimeline();
												}
											});
									recyclableTimelines.add(timeline);
								} catch (Exception e) {
									LOGGER.fatal(
											"Error creating timeline. The "
													+ timelineGroup.getClass()
															.getSimpleName()
													+ " is in an inconsistent state.",
											e);
								}
							}
						} else if (unpreparedKeys.size() < recyclableTimelines
								.size()) {
							final int toManyTimelines = recyclableTimelines
									.size() - unpreparedKeys.size();
							ExecUtils.syncExec(new Runnable() {
								@Override
								public void run() {
									for (int i = 0; i < toManyTimelines; i++) {
										recyclableTimelines.remove(0).dispose();
									}
								}
							});
						}

						passed.tell("timeline created/disposed");

						/*
						 * Invariant: There are as many recyclableTimelines as
						 * unpreparedKeys.
						 */
						Assert.isTrue(unpreparedKeys.size() == recyclableTimelines
								.size());

						/*
						 * Prepare unpreparated keys
						 */
						Map<INPUT, Asset<TIMELINE, INPUT>> preparedKeys = new HashMap<INPUT, Asset<TIMELINE, INPUT>>();
						for (int i = 0, m = unpreparedKeys.size(); i < m; i++) {
							final INPUT unpreparedKey = unpreparedKeys
									.remove(unpreparedKeys.size() - 1);
							final TIMELINE recyclableTimeline = recyclableTimelines
									.remove(recyclableTimelines.size() - 1);
							ExecUtils.syncExec(new Runnable() {
								@Override
								public void run() {
									recyclableTimeline.setData(unpreparedKey);
								}
							});
							ITimelineProvider<TIMELINE, INPUT> timelineProvider = MinimalTimelineGroupViewer.this.timelineProviderFactory
									.createTimelineProvider();
							MinimalTimelineGroupViewer.this.notifyInputChanged(
									timelineProvider, null, unpreparedKey);
							preparedKeys.put(unpreparedKey,
									new Asset<TIMELINE, INPUT>(
											recyclableTimeline,
											timelineProvider));
						}

						passed.tell("keys prepared");

						Assert.isTrue(unpreparedKeys.isEmpty());
						Assert.isTrue(recyclableTimelines.isEmpty());
						Assert.isTrue(MinimalTimelineGroupViewer.this.loadedKeys
								.keySet().size() + preparedKeys.keySet().size() == neededKeys
								.size());
						Assert.isTrue(CollectionUtils.isEqualCollection(
								CollectionUtils
										.union(MinimalTimelineGroupViewer.this.loadedKeys
												.keySet(), preparedKeys
												.keySet()), neededKeys));

						MinimalTimelineGroupViewer.this.refresh(
								MinimalTimelineGroupViewer.this.loadedKeys,
								false, subMonitor.newChild(1));
						passed.tell("loaded keys refreshed");
						MinimalTimelineGroupViewer.this.refresh(preparedKeys,
								true, subMonitor.newChild(1));
						passed.tell("prepared keys refreshed");

						MinimalTimelineGroupViewer.this.loadedKeys
								.putAll(preparedKeys);

						return ExecUtils.asyncExec(new Runnable() {
							@Override
							public void run() {
								timelineGroup.layout();
								passed.tell("layout completed");
							}
						}).get();
					}
				});

	}

	// FIXME: make this function abort itself on consecutive calls
	// different demoAreaContent providers can make this method be called
	// multiple times
	// because of one core event
	// FIXME: TimelineRefresher no more needed then
	private Future<Void> refresh(Map<INPUT, Asset<TIMELINE, INPUT>> keys,
			final boolean keysAreNew, final IProgressMonitor monitor) {
		final List<Future<Void>> futures = new ArrayList<Future<Void>>();

		final TimePassed passed = new TimePassed("TIMELINE INNER REFRESH");
		SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
		for (final Object key : keys.keySet()) {
			try {
				TIMELINE timeline = keys.get(key).getTimeline();
				passed.tell("got timeline");
				ITimelineProvider<TIMELINE, INPUT> timelineProvider = keys.get(
						key).getTimelineProvider();
				passed.tell("got timeline provider");

				ITimelineInput timelineInput = timelineProvider
						.generateTimelineInput(
								timeline,
								new ITimelineLabelProviderCreationInterceptor() {
									@Override
									public void postProcess(
											Object businessObject,
											ITimelineEvent event) {
										MinimalTimelineGroupViewer.this
												.postProcess(businessObject,
														event, keysAreNew);
									}
								}, subMonitor.newChild(1));

				passed.tell("created input");

				this.postProcess(timeline, timelineInput, keysAreNew);

				passed.tell("post processed");

				Future<Void> future;
				if (keysAreNew) {
					future = timeline.show(timelineInput, 300, 300,
							subMonitor.newChild(2));
				} else {
					future = timeline.show(timelineInput,
							subMonitor.newChild(2));
				}

				futures.add(future);
			} catch (Exception e) {
				LOGGER.error("Error refreshing timeline for key " + key, e);
			}
		}

		if (futures.size() > 0) {
			return ExecUtils.nonUIAsyncExec(MinimalTimelineGroupViewer.class,
					"Waiting for timeline show to complete",
					new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							for (Future<Void> future : futures) {
								future.get();
							}
							passed.tell("timelines displayed");
							return null;
						}
					});
		} else {
			return new CompletedFuture<Void>(null, null);
		}
	}

	protected TIMELINE getTimeline(Object key) {
		return this.loadedKeys.get(key).getTimeline();
	}

	/**
	 * This method gives inheriting classes the possibility to modify a created
	 * {@link ITimelineEvent} before it becomes part of the
	 * {@link ITimelineInput}.
	 * 
	 * @param businessObject
	 * @param event
	 * @param inputIsNew
	 *            true if the business object has not been loaded in the
	 *            timeline instance before
	 */
	protected void postProcess(Object businessObject, ITimelineEvent event,
			boolean inputIsNew) {
		// do nothing
	}

	/**
	 * This method gives inheriting classes the possibility to modify the
	 * timeline and/or input before the input gets loaded.
	 * 
	 * @param timeline
	 * @param input
	 * @param inputIsNew
	 *            true if the input has not been loaded in the timeline instance
	 *            before
	 */
	protected void postProcess(TIMELINE timeline, ITimelineInput input,
			boolean inputIsNew) {
		// do nothing
	}

}
