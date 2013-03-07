package com.bkahlert.devel.nebula.widgets.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.composer.Composer;

/**
 * Instances of this class wrap a {@link Composer} and at load and save
 * functionality.
 * 
 * @author bkahlert
 * 
 */
public abstract class Editor<T> extends Composite {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Editor.class);

	private T loadedObject = null;
	private Job loadJob = null;
	private Map<T, Job> saveJobs = new HashMap<T, Job>();

	Composer composer;

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param delayChangeEventUpTo
	 *            is the delay that must have been passed in order save the
	 *            currently loaded object. If 0 no delay will be applied. The
	 *            minimal delay however defined by the wrapped {@link Composer}.
	 */
	public Editor(Composite parent, int style, final long delayChangeEventUpTo) {
		super(parent, style);

		this.setLayout(new FillLayout());
		this.composer = new Composer(this, SWT.NONE, delayChangeEventUpTo);
		this.composer.setEnabled(false);
	}

	@Override
	public boolean setFocus() {
		return this.composer.setFocus();
	}

	public T getLoadedObject() {
		return loadedObject;
	}

	/**
	 * Loads the given object and immediately returns.
	 * <p>
	 * May safely be called multiple times. If this {@link Editor} is still
	 * loading the current load job is cancelled.
	 * 
	 * @ArbitraryThread may be called from whatever thread you like.
	 * @param objectToLoad
	 * @throws Exception
	 */
	public final void load(final T objectToLoad) {
		if (loadJob != null)
			loadJob.cancel();

		if (objectToLoad == null) {
			// refreshHeader();
			loadedObject = null;
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					composer.setSource("");
					composer.setEnabled(false);
				}
			});
		} else {
			loadJob = new Job("Loading " + objectToLoad) {
				@Override
				protected IStatus run(IProgressMonitor progressMonitor) {
					if (progressMonitor.isCanceled()) {
						loadedObject = null;
						return Status.CANCEL_STATUS;
					}
					SubMonitor monitor = SubMonitor.convert(progressMonitor, 3);
					final String html = getHtml(objectToLoad,
							monitor.newChild(1));
					// refreshHeader();
					monitor.worked(1);
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							composer.setSource(html);
							composer.setEnabled(true);
						}
					});
					monitor.worked(1);
					monitor.done();
					loadedObject = objectToLoad;
					return Status.OK_STATUS;
				}
			};
			loadJob.schedule();
		}
	}

	/**
	 * Saves the current content of the {@link Editor} to the loaded object.
	 * 
	 * @throws Exception
	 */
	public void save() throws Exception {
		String html = ExecutorUtil.syncExec(new Callable<String>() {
			@Override
			public String call() {
				return composer.getSource();
			}
		});
		this.save(html);
	}

	/**
	 * Saves the given html to the loaded object.
	 * <p>
	 * In contrast to {@link #save()} this method does not use the
	 * {@link Editor}'s content and thus also works if this widget is being
	 * disposed.
	 * 
	 * @param html
	 */
	synchronized final void save(final String html) {
		final T savedLoadedObject = loadedObject;

		if (saveJobs.get(savedLoadedObject) != null)
			saveJobs.get(savedLoadedObject).cancel();

		// make the loaded object still accessible even if another one has been
		// loaded already so the save job can finish

		Job saveJob = new Job("Saving " + savedLoadedObject) {
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				if (progressMonitor.isCanceled())
					return Status.CANCEL_STATUS;
				SubMonitor monitor = SubMonitor.convert(progressMonitor, 2);
				monitor.worked(1);
				setHtml(savedLoadedObject, html, monitor.newChild(1));
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		saveJob.schedule();
		saveJobs.put(savedLoadedObject, saveJob);
	}

	/**
	 * Returns the html for the given object.
	 * 
	 * @param objectToLoad
	 * @param monitor
	 * @return
	 */
	public abstract String getHtml(T objectToLoad, IProgressMonitor monitor);

	/**
	 * Sets the given html to the loaded object.
	 * 
	 * @param loadedObject
	 * @param html
	 * @param monitor
	 */
	public abstract void setHtml(T loadedObject, String html,
			IProgressMonitor monitor);

}
