package com.bkahlert.nebula.viewer.timeline.provider.complex;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;

/**
 * Instances of this class encapsulate the providers needed to render one
 * timeline.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineProvider<TIMELINE extends IBaseTimeline, INPUT> {

	/**
	 * Instances of this class can be used to influence the
	 * {@link ITimelineInput} creation process.
	 * 
	 * @author bkahlert
	 * 
	 */
	public interface ITimelineLabelProviderCreationInterceptor {
		public void postProcess(Object businessObject, ITimelineEvent event);
	}

	/**
	 * Returns the {@link ITimelineLabelProvider} that is responsible to label
	 * the timeline this {@link ITimelineProvider} is responsible for.
	 * 
	 * @return
	 */
	public ITimelineLabelProvider<TIMELINE> getTimelineLabelProvider();

	/**
	 * Returns the {@link IBandGroupProvider}s that are responsible to provide
	 * the bands of the timeline this {@link ITimelineProvider} is responsible
	 * for.
	 * 
	 * @return
	 */
	public List<IBandGroupProvider<INPUT>> getBandGroupProviders();

	/**
	 * Generates an {@link ITimelineInput} object that can serve as the input
	 * for a {@link IBaseTimeline}.
	 * 
	 * @param timeline
	 *            that is used to compute the timeline labels
	 * @param monitor
	 * @return
	 */
	public ITimelineInput generateTimelineInput(TIMELINE timeline,
			IProgressMonitor monitor);

	/**
	 * Generates an {@link ITimelineInput} object that can serve as the input
	 * for a {@link IBaseTimeline}.
	 * 
	 * @param timeline
	 *            that is used to compute the timeline labels
	 * @param creationInterceptor
	 * @param monitor
	 * @return
	 */
	public ITimelineInput generateTimelineInput(TIMELINE timeline,
			ITimelineLabelProviderCreationInterceptor creationInterceptor,
			IProgressMonitor monitor);

}