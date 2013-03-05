package com.bkahlert.devel.nebula.viewer.timelineGroup.impl;

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
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider.ITimelineLabelProviderCreationInterceptor;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.viewer.timelineGroup.ITimelineGroupViewer;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

/**
 * This class implements a minimal implementation of
 * {@link ITimelineGroupViewer}.
 * <p>
 * Timelines are economically used. They are only created if necessary. No more
 * needed timelines are used to load another key if no more needed.
 * 
 * @author bkahlert
 * 
 * @param <TIMELINEGROUP>
 * @param <TIMELINE>
 */
public class MinimalTimelineGroupViewer<TIMELINEGROUP extends ITimelineGroup<TIMELINE>, TIMELINE extends ITimeline>
		extends AbstractTimelineGroupViewer<TIMELINEGROUP> {

	private static final Logger LOGGER = Logger
			.getLogger(MinimalTimelineGroupViewer.class);

	private static class Asset<TIMELINE extends IBaseTimeline> {
		private TIMELINE timeline;
		private ITimelineProvider<TIMELINE> timelineProvider;

		public Asset(TIMELINE timeline,
				ITimelineProvider<TIMELINE> timelineProvider) {
			super();
			this.timeline = timeline;
			this.timelineProvider = timelineProvider;
		}

		public TIMELINE getTimeline() {
			return timeline;
		}

		public ITimelineProvider<TIMELINE> getTimelineProvider() {
			return timelineProvider;
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
		if (input == null)
			return null;
		if (input.getClass().isArray())
			return (Object[]) input;
		if (input instanceof Collection<?>)
			return ((Collection<?>) input).toArray();
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

	private ITimelineProviderFactory<TIMELINE> timelineProviderFactory;
	private Object rawInput;

	/**
	 * Contains completely loaded keys and their {@link Asset}s.
	 */
	private Map<Object, Asset<TIMELINE>> loadedKeys = new HashMap<Object, Asset<TIMELINE>>();

	public MinimalTimelineGroupViewer(TIMELINEGROUP timelineGroup,
			ITimelineProviderFactory<TIMELINE> timelineProviderFactory) {
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
			ITimelineProvider<TIMELINE> timelineProvider, Object oldInput,
			Object newInput) {
		for (IBandGroupProvider bandGroupProvider : timelineProvider
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
		@SuppressWarnings("unchecked")
		final TIMELINEGROUP timelineGroup = (TIMELINEGROUP) getControl();

		List<Object> neededKeys = new ArrayList<Object>(
				Arrays.asList(breakUp(rawInput)));

		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				neededKeys.size());

		final List<TIMELINE> recyclableTimelines = new ArrayList<TIMELINE>();
		for (Iterator<Entry<Object, Asset<TIMELINE>>> iterator = loadedKeys
				.entrySet().iterator(); iterator.hasNext();) {
			final Entry<Object, Asset<TIMELINE>> loaded = iterator.next();
			if (!neededKeys.contains(loaded.getKey())) {
				ExecutorUtil.syncExec(new Runnable() {
					@Override
					public void run() {
						TIMELINE timeline = loaded.getValue().getTimeline();
						ITimelineProvider<TIMELINE> provider = loaded
								.getValue().getTimelineProvider();
						notifyInputChanged(provider, timeline.getData(), null);
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
		List<Object> unpreparedKeys = new ArrayList<Object>(
				CollectionUtils.subtract(neededKeys, loadedKeys.keySet()));

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
					for (int i = 0; i < toManyTimelines; i++)
						recyclableTimelines.remove(0).dispose();
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
		Map<Object, Asset<TIMELINE>> preparedKeys = new HashMap<Object, Asset<TIMELINE>>();
		for (int i = 0, m = unpreparedKeys.size(); i < m; i++) {
			final Object unpreparedKey = unpreparedKeys.remove(unpreparedKeys
					.size() - 1);
			final TIMELINE recyclableTimeline = recyclableTimelines
					.remove(recyclableTimelines.size() - 1);
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					recyclableTimeline.setData(unpreparedKey);
				}
			});
			ITimelineProvider<TIMELINE> timelineProvider = this.timelineProviderFactory
					.createTimelineProvider();
			notifyInputChanged(timelineProvider, null, unpreparedKey);
			preparedKeys.put(unpreparedKey, new Asset<TIMELINE>(
					recyclableTimeline, timelineProvider));
		}

		Assert.isTrue(unpreparedKeys.isEmpty());
		Assert.isTrue(recyclableTimelines.isEmpty());
		Assert.isTrue(loadedKeys.keySet().size() + preparedKeys.keySet().size() == neededKeys
				.size());
		Assert.isTrue(CollectionUtils.isEqualCollection(
				CollectionUtils.union(loadedKeys.keySet(),
						preparedKeys.keySet()), neededKeys));

		refresh(loadedKeys, false, subMonitor.newChild(1));
		refresh(preparedKeys, true, subMonitor.newChild(1));

		loadedKeys.putAll(preparedKeys);

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
	public void refresh(Map<Object, Asset<TIMELINE>> keys,
			final boolean keysAreNew, final IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
		for (final Object key : keys.keySet()) {
			try {
				TIMELINE timeline = keys.get(key).getTimeline();
				ITimelineProvider<TIMELINE> timelineProvider = keys.get(key)
						.getTimelineProvider();

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

				postProcess(timeline, timelineInput, keysAreNew);

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
