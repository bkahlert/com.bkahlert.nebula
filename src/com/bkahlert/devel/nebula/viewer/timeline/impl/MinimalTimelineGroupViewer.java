package com.bkahlert.devel.nebula.viewer.timeline.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider.ITimelineLabelProviderCreationInterceptor;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineGroup;
import com.bkahlert.devel.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;

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
public class MinimalTimelineGroupViewer<TIMELINEGROUP extends TimelineGroup<TIMELINE>, TIMELINE extends ITimeline, INPUT>
		extends AbstractTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT> {

	private static final Logger LOGGER = Logger
			.getLogger(MinimalTimelineGroupViewer.class);

	private static class Asset<TIMELINEGROUPVIEWER extends MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP extends TimelineGroup<TIMELINE>, TIMELINE extends ITimeline, INPUT> {
		private TIMELINE timeline;
		private ITimelineProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> timelineProvider;

		public Asset(
				TIMELINE timeline,
				ITimelineProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> timelineProvider) {
			super();
			this.timeline = timeline;
			this.timelineProvider = timelineProvider;
		}

		public TIMELINE getTimeline() {
			return this.timeline;
		}

		public ITimelineProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> getTimelineProvider() {
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
	public static Object[] breakUp(Object input) {
		if (input == null) {
			return null;
		}
		if (input.getClass().isArray()) {
			return (Object[]) input;
		}
		if (input instanceof Collection<?>) {
			return ((Collection<?>) input).toArray();
		}
		return new Object[] { input };
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

	private ITimelineProviderFactory<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT> timelineProviderFactory;
	private Object rawInput;

	/**
	 * Contains completely loaded keys and their {@link Asset}s.
	 */
	private Map<INPUT, Asset<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT>> loadedKeys = new HashMap<INPUT, Asset<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT>>();

	public MinimalTimelineGroupViewer(
			TIMELINEGROUP timelineGroup,
			ITimelineProviderFactory<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT> timelineProviderFactory) {
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

	protected void notifyInputChanged(
			ITimelineProvider<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT> timelineProvider,
			INPUT oldInput, INPUT newInput) {
		for (IBandGroupProvider<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : timelineProvider
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
	public void refresh(IProgressMonitor monitor) {
		final TIMELINEGROUP timelineGroup = this.getControl();

		List<Object> neededKeys = new ArrayList<Object>(
				Arrays.asList(breakUp(this.rawInput)));

		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				neededKeys.size());

		final List<TIMELINE> recyclableTimelines = new ArrayList<TIMELINE>();
		for (Iterator<Entry<INPUT, Asset<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT>>> iterator = this.loadedKeys
				.entrySet().iterator(); iterator.hasNext();) {
			final Entry<INPUT, Asset<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT>> loaded = iterator
					.next();
			if (!neededKeys.contains(loaded.getKey())) {
				ExecutorUtil.syncExec(new Runnable() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						TIMELINE timeline = loaded.getValue().getTimeline();
						ITimelineProvider<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT> provider = loaded
								.getValue().getTimelineProvider();
						MinimalTimelineGroupViewer.this.notifyInputChanged(
								provider, (INPUT) timeline.getData(), null);
						loaded.getValue().getTimeline().setData(null);
						recyclableTimelines.add(timeline);
					}
				});
				iterator.remove();
			}
		}

		/*
		 * Invariant: (1) loadedKeys only contains actually needed and loaded
		 * key (2) recyclableTimelines contains timelines that can be reused
		 */
		@SuppressWarnings("unchecked")
		List<INPUT> unpreparedKeys = new ArrayList<INPUT>(
				CollectionUtils.subtract(neededKeys, this.loadedKeys.keySet()));

		if (unpreparedKeys.size() > recyclableTimelines.size()) {
			int missingTimelines = unpreparedKeys.size()
					- recyclableTimelines.size();
			for (int i = 0; i < missingTimelines; i++) {
				try {
					TIMELINE timeline = ExecutorUtil
							.syncExec(new Callable<TIMELINE>() {
								@Override
								public TIMELINE call() throws Exception {
									return timelineGroup.createTimeline();
								}
							});
					recyclableTimelines.add(timeline);
				} catch (Exception e) {
					LOGGER.fatal("Error creating timeline. The "
							+ timelineGroup.getClass().getSimpleName()
							+ " is in an inconsistent state.", e);
				}
			}
		} else if (unpreparedKeys.size() < recyclableTimelines.size()) {
			final int toManyTimelines = recyclableTimelines.size()
					- unpreparedKeys.size();
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < toManyTimelines; i++) {
						recyclableTimelines.remove(0).dispose();
					}
				}
			});
		}

		/*
		 * Invariant: There are as many recyclableTimelines as unpreparedKeys.
		 */
		Assert.isTrue(unpreparedKeys.size() == recyclableTimelines.size());

		/*
		 * Prepare unpreparated keys
		 */
		Map<INPUT, Asset<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT>> preparedKeys = new HashMap<INPUT, Asset<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT>>();
		for (int i = 0, m = unpreparedKeys.size(); i < m; i++) {
			final INPUT unpreparedKey = unpreparedKeys.remove(unpreparedKeys
					.size() - 1);
			final TIMELINE recyclableTimeline = recyclableTimelines
					.remove(recyclableTimelines.size() - 1);
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					recyclableTimeline.setData(unpreparedKey);
				}
			});
			ITimelineProvider<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT> timelineProvider = this.timelineProviderFactory
					.createTimelineProvider();
			this.notifyInputChanged(timelineProvider, null, unpreparedKey);
			preparedKeys
					.put(unpreparedKey,
							new Asset<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT>(
									recyclableTimeline, timelineProvider));
		}

		Assert.isTrue(unpreparedKeys.isEmpty());
		Assert.isTrue(recyclableTimelines.isEmpty());
		Assert.isTrue(this.loadedKeys.keySet().size()
				+ preparedKeys.keySet().size() == neededKeys.size());
		Assert.isTrue(CollectionUtils.isEqualCollection(
				CollectionUtils.union(this.loadedKeys.keySet(),
						preparedKeys.keySet()), neededKeys));

		this.refresh(this.loadedKeys, false, subMonitor.newChild(1));
		this.refresh(preparedKeys, true, subMonitor.newChild(1));

		this.loadedKeys.putAll(preparedKeys);

		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				timelineGroup.layout();
			}
		});
	}

	// FIXME: make this function abort itself on consecutive calls
	// different content providers can make this method be called multiple times
	// because of one core event
	// FIXME: TimelineRefresher no more needed then
	public void refresh(
			Map<INPUT, Asset<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT>> keys,
			final boolean keysAreNew, final IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
		for (final Object key : keys.keySet()) {
			try {
				TIMELINE timeline = keys.get(key).getTimeline();
				ITimelineProvider<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT> timelineProvider = keys
						.get(key).getTimelineProvider();

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

				this.postProcess(timeline, timelineInput, keysAreNew);

				if (keysAreNew) {
					timeline.show(timelineInput, 300, 300,
							subMonitor.newChild(2));
				} else {
					timeline.show(timelineInput, subMonitor.newChild(2));
				}
			} catch (Exception e) {
				LOGGER.error("Error refreshing timeline for key " + key, e);
			}
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
