package com.bkahlert.devel.nebula.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A job that changes it's {@link Thread}'s name.
 * 
 * @author bkahlert
 * 
 */
public abstract class NamedJob extends Job {

	private final Class<?> clazz;
	private final String name;

	public NamedJob(Class<?> clazz, String name, String progressCaption) {
		super(progressCaption);
		this.clazz = clazz;
		this.name = name;
	}

	public NamedJob(Class<?> clazz, String progressCaption) {
		super(progressCaption);
		this.clazz = clazz;
		this.name = progressCaption;
	}

	@Override
	final protected IStatus run(final IProgressMonitor monitor) {
		ExecUtils.backupThreadLabel();
		ExecUtils.setThreadLabel("JOB :: ", this.clazz, this.name);
		try {
			return NamedJob.this.runNamed(monitor);
		} finally {
			ExecUtils.restoreThreadLabel();
		}
	}

	protected abstract IStatus runNamed(IProgressMonitor monitor);

}
