package com.bkahlert.devel.nebula.widgets.timeline.impl;

import org.apache.log4j.Logger;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

public class TimePassed {

	private final Logger logger;
	private final long start;
	private long lastTell;
	private final String prefix0 = "TIME PASSED MEASUREMENT";
	private final String prefix;

	public TimePassed() {
		this.logger = null;
		this.start = this.lastTell = System.currentTimeMillis();
		this.prefix = null;
		System.out.println(prefix0 + " :: " + thread() + " :: started");
	}

	public TimePassed(Logger logger) {
		this.logger = logger;
		this.start = this.lastTell = System.currentTimeMillis();
		this.prefix = null;
		logger.debug(prefix0 + " :: " + thread() + " :: started");
	}

	public TimePassed(String prefix) {
		this.logger = null;
		this.start = this.lastTell = System.currentTimeMillis();
		this.prefix = prefix;
		System.out.println(prefix0 + " :: " + thread() + " :: " + prefix
				+ " :: started");
	}

	public TimePassed(String prefix, Logger logger) {
		this.logger = logger;
		this.start = this.lastTell = System.currentTimeMillis();
		this.prefix = prefix;
		logger.debug(prefix0 + " :: " + thread() + " :: " + prefix
				+ " :: started");
	}

	private String thread() {
		return ExecutorUtil.isUIThread() ? "UI thread" : Thread.currentThread()
				.toString();
	}

	public void tell(String event) {
		String message = prefix0 + " :: " + thread() + " :: " + prefix + " :: "
				+ event + " :: " + (System.currentTimeMillis() - start)
				+ "ms (+" + (System.currentTimeMillis() - lastTell) + "ms)";
		lastTell = System.currentTimeMillis();
		if (logger != null) {
			logger.debug(message);
		} else {
			System.out.println(message);
		}
	}

	public void finished() {
		tell("finished");
	}

}
