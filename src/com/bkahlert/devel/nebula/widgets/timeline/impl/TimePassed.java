package com.bkahlert.devel.nebula.widgets.timeline.impl;

import org.apache.log4j.Logger;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

public class TimePassed {

	private final boolean silent;

	private final Logger logger;
	private final long start = System.currentTimeMillis();
	private long lastTell = -1;
	private final String prefix0 = "TIME PASSED MEASUREMENT";
	private final String prefix;

	public TimePassed(boolean silent) {
		this.silent = silent;
		this.logger = null;
		this.prefix = null;
	}

	public TimePassed() {
		this(false);
	}

	public TimePassed(boolean silent, Logger logger) {
		this.silent = silent;
		this.logger = logger;
		this.prefix = null;
	}

	public TimePassed(Logger logger) {
		this(false, logger);
	}

	public TimePassed(boolean silent, String prefix) {
		this.silent = silent;
		this.logger = null;
		this.prefix = prefix;
		if (!silent) {
			System.out.println(prefix0 + " :: " + thread() + " :: " + prefix
					+ " :: started");
		}
	}

	public TimePassed(String prefix) {
		this(false, prefix);
	}

	public TimePassed(boolean silent, String prefix, Logger logger) {
		this.silent = silent;
		this.logger = logger;
		this.prefix = prefix;
		if (!silent) {
			logger.debug(prefix0 + " :: " + thread() + " :: " + prefix
					+ " :: started");
		}
	}

	public TimePassed(String prefix, Logger logger) {
		this(true, prefix, logger);
	}

	private String thread() {
		return ExecutorUtil.isUIThread() ? "UI thread" : Thread.currentThread()
				.toString();
	}

	public void tell(String event) {
		String message = prefix0 + " :: " + thread() + " :: " + prefix + " :: "
				+ event + " :: " + getTimePassed() + "ms";
		if (lastTell >= 0) {
			message += " (+" + (System.currentTimeMillis() - lastTell) + "ms)";
		}
		lastTell = System.currentTimeMillis();
		if (!silent) {
			if (logger != null) {
				logger.debug(message);
			} else {
				System.out.println(message);
			}
		}
	}

	public long getTimePassed() {
		return System.currentTimeMillis() - start;
	}

	public void finished() {
		if (!silent) {
			tell("finished");
		}
	}

}
