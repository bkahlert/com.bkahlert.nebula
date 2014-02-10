package com.bkahlert.devel.nebula.widgets.timeline.impl;

import org.apache.log4j.Logger;

import com.bkahlert.devel.nebula.utils.ExecUtils;

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
			System.out.println(this.prefix0 + " :: " + this.thread() + " :: "
					+ prefix + " :: started");
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
			logger.debug(this.prefix0 + " :: " + this.thread() + " :: "
					+ prefix + " :: started");
		}
	}

	public TimePassed(String prefix, Logger logger) {
		this(true, prefix, logger);
	}

	private String thread() {
		return ExecUtils.isUIThread() ? "UI thread" : Thread.currentThread()
				.toString();
	}

	public void tell(String event) {
		String message = this.prefix0 + " :: " + this.thread() + " :: "
				+ this.prefix + " :: " + event + " :: " + this.getTimePassed()
				+ "ms";
		if (this.lastTell >= 0) {
			message += " (+" + (System.currentTimeMillis() - this.lastTell)
					+ "ms)";
		}
		this.lastTell = System.currentTimeMillis();
		if (!this.silent) {
			if (this.logger != null) {
				this.logger.debug(message);
			} else {
				System.out.println(message);
			}
		}
	}

	public long getTimePassed() {
		return System.currentTimeMillis() - this.start;
	}

	public void finished() {
		if (!this.silent) {
			this.tell("finished");
		}
	}

}
